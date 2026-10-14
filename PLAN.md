# Flywheel Vulkan 后端 —— 实施计划

状态：**阶段 0–3 已完成并验证**，阶段 4 起为待实施部分。
分支：`ZurrTum/Create-Fly` @ `release` (= `1.21.11_obfuscation`) HEAD `8bf66be4`

---

## 一、已完成并验证的阶段

### 阶段 0 —— 构建环境 ✅

| 项 | 结果 |
|---|---|
| 源码 | ZurrTum/Create-Fly，分支 `1.21.11_obfuscation`（**不是** `1.21.11` 开发分支） |
| 工具链 | Gradle 9.2.1 + Fabric Loom 1.15.5 + `remapJar` |
| 产物 | `create-fly-1.21.11-6.0.9-5.jar`，intermediary 命名空间 |
| 验证 | 与线上 Create jar 的 587 个 flywheel 条目**逐字节一致，差异 0** |
| 部署 | `deploy.ps1`：备份 → 构建 → `jar uf` 替换 flywheel 包 → 校验 → 可 `-Rollback` |

踩过的坑（都已解决）：
1. Java 不读 Windows 系统代理 → 直连撞 PKIX。用 `systemProp.*Proxy*` 绕过。
2. 克隆了开发分支，产物是 named 映射且无 `remapJar` → 装上去必炸。
3. 发布分支的 `DrawManager` 比开发分支多一个抽象方法 `meshPool()`。

### 阶段 3 —— 注册链路 ✅

```
[FW/VK] supported() consulted: backend='Vulkan' vendor='Nvidia'
    renderer='NVIDIA GeForce RTX 4060 Laptop GPU' vulkanmodLoaded=true
    isVulkan=true -> usable=true (Phase 3 hardcoded false)
Flywheel backend fell back from 'flywheel:vulkan' to 'flywheel:off'
```

改动范围经逐条目比对确认：新增 3 条（`vulkan/` 包 + 2 个 class），修改 1 条（`Backends.class`），删除 0 条，其余 14970 条未动。

---

## 二、阶段 1 的四个决定性发现

这四条彻底改变了技术路径。

### 发现 1：Flywheel 的 shader 是运行时生成的 —— 不需要资源化

`PipelineCompiler` 把 GLSL 片段拼接成完整 shader 后编译：

```java
PIPELINE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.VERTEX)
    .withResource(API_IMPL_VERT)                                        // 资源片段
    .withComponent(key -> new InstanceStructComponent(key.instanceType()))  // 生成
    .withResource(key -> key.instanceType().vertexShader())             // 资源片段
    .withResource(key -> key.materialShaders().vertexSource())         // 资源片段
    .withComponent(key -> pipeline.assembler().assemble(key.instanceType())) // 代码生成
    .withResource(pipeline.vertexMain())
```

**推翻了我最初的担心**：曾以为必须把 52 个 GLSL 转成 Minecraft 的资源形式、并适配 `RenderPipeline` 那个「`amo` 是资源标识符」的约束。**完全不需要**。Flywheel 压根不走那套系统。

shader 缓存键是 7 维的 `PipelineProgramKey`：
`instanceType × contextShader × light × materialShaders × useCutout × debug × oitMode`

### 发现 2：VulkanMod 已经提供了完整的 shader 管线 API

这是最关键的利好 —— 不用自己碰 SPIR-V：

```java
// net.vulkanmod.vulkan.shader.Pipeline$Builder
new Pipeline$Builder(VertexFormat)
    .setShaderSrc(ShaderKind.VERTEX, glslSourceString)   // ← 直接喂 GLSL 源码字符串
    .setShaderSrc(ShaderKind.FRAGMENT, glslSourceString)
    .addUBO(UBO)                                         // uniform buffer
    .addImageDescriptor(ImageDescriptor)                 // 纹理采样器
    .applyConfig(PipelineConfig)
    .createGraphicsPipeline()                            // → GraphicsPipeline

// net.vulkanmod.vulkan.shader.SPIRVUtils
SPIRVUtils.compileShader(source, name, kind)   // shaderc，GLSL → SPIR-V
SPIRVUtils.addIncludePath(path)                // include 支持

// GraphicsPipeline
getHandle(PipelineState) → VkPipeline
```

依赖里已有 `org.lwjgl:lwjgl-shaderc` 3.4.x（含 Windows 原生库），**不需要任何外部工具链**。

**结论：保留 Flywheel 整套 GLSL 生成器，只替换最后一步「用 GL 编译」→「交给 Pipeline.Builder」。**

### 发现 3：实例数据存在纹理里，不是 VBO

```java
private final TextureBuffer instanceTexture;
Samplers.INSTANCE_BUFFER.makeActive();
drawCall.render(instanceTexture);
```

**这是利好。** 因为 MC 的 `RenderPass.bindTexture(String, GpuTextureView, SamplerSetup)` 原生支持贴图绑定，走纹理路线可以绕开 `getMaxUniformOffsetAlignment()` 那套对齐问题，也不用设计 instance attribute 布局。

### 发现 4：OIT 是五连 pass，v1 建议整体跳过

```java
oitFramebuffer.prepare();
oitFramebuffer.depthRange();
submitOitDraws(DEPTH_RANGE);
oitFramebuffer.renderTransmittance();
submitOitDraws(GENERATE_COEFFICIENTS);
oitFramebuffer.renderDepthFromTransmittance();
oitFramebuffer.accumulate();
submitOitDraws(EVALUATE);
oitFramebuffer.composite();
```

有序无关透明是个完整子系统。**v1 只做不透明路径**，半透明部件降级成普通渲染。

---

## 三、已确认可用的 MC / VulkanMod 渲染接口

```java
// 设备
GpuDevice  createBuffer(name, usage, size) / createCommandEncoder() / getMaxUniformOffsetAlignment()

// 提交
CommandEncoder.createRenderPass(color, OptionalInt, depth, OptionalDouble)
RenderPass.setPipeline / setVertexBuffer(slot, GpuBuffer) / setIndexBuffer
          / drawIndexed(int,int,int,int)
          / drawMultipleIndexed(Collection<DrawCommand>, GpuBuffer, IndexType, Collection<String>, T)
          / setUniform(String, GpuBufferSlice) / bindTexture(String, GpuTextureView, SamplerSetup)
          / enableScissor / close()
```

---

## 四、实施阶段（阶段 4 起）

### 阶段 4：GL 依赖面盘点 ⚠️ 先做这个

在动手前把 `backend/gl`（21 文件）和 `backend/glsl`（34 文件）里**真正需要替换的部分**列清楚，哪些能复用、哪些必须重写。

产出：`GL-REPLACEMENT-MAP.md` —— 逐类标注 `复用 / 替换 / 延后`。

验收：清单覆盖 `GlCompat`、`GlProgram`、`TextureBuffer`、`GlVertexArray`、`TextureBinder`、`MaterialRenderState`、`GlStateTracker`、`Uniforms`。

工作量：0.5 天。

---

### 阶段 5：缓冲层 —— `GpuTexture` / `GpuBuffer` 替换 GL 对象

要改：`TextureBuffer` → 用 `GpuTexture` + `GpuTextureView`；`GlVertexArray` → `GpuBuffer` + `setVertexBuffer`；`MeshPool` 保留但换底层上传。

要点：
- 纹理创建：`device.createTexture(name, usage, TextureFormat, mipLevel, width, height, layers)`
- 数据上传：`encoder.writeToTexture(...)` 或 `mapBuffer`
- 生命周期：instancer 删除时释放

验收：能上传并读回校验；Create 的机器数量正确。

工作量：2–3 天。

---

### 阶段 6：Shader 编译层 ★核心

**GLSL 生成器一行不改**，只替换 `GlProgram` 为 `VkProgram`：

```java
class VkProgram {
    // 复用 Flywheel 生成的 GLSL 文本
    // → Pipeline.Builder.setShaderSrc()
    // → createGraphicsPipeline()
}
```

要处理的映射：

| GL 概念 | Vulkan 对应 | 备注 |
|---|---|---|
| `bindAttribLocation("_flw_aPos", 0..5)` | `Pipeline.Builder(VertexFormat)` | VertexFormat 已封装 |
| `setSamplerBinding(name, Samplers.X)` | `addImageDescriptor(ImageDescriptor)` | 7 个 sampler |
| `Uniforms.setUniformBlockBindings(program)` | `addUBO(UBO)` | UBO 布局 |
| `program.setUInt/setInt/setUVec2` | `setUniform(name, GpuBufferSlice)` | 材质参数 |
| `GlProgram.unbind()` | 无需 | Vulkan 无全局状态 |

**已知难点**：sampler 数量随 `oitMode` 变化（OIT 需要 `_flw_depthRange`、`_flw_coefficients`）。**descriptor set 布局必须按 key 动态生成**，不能固定。

另外要确认 Flywheel 的 GLSL 片段拼接用不用 `#include` —— 若用，需要 `SPIRVUtils.addIncludePath()` 注册路径。

验收：不崩溃，shader 编译成功（可用 RenderDoc 抓帧确认）。

工作量：3–5 天。**这是最大不确定项。**

---

### 阶段 7：Uniform / 光照

`EngineImpl.render()` 现在包在 `GlStateTracker.getRestoreState()` 里；`Uniforms.update(context)`、`LightStorage`、`EnvironmentStorage` 都要改成喂 `GpuBuffer`。

要点：`FrameUniforms`、`LightStorage` 的光照数据怎么打包成 uniform buffer。

验收：机器在明暗环境下都正确。

工作量：2 天。

---

### 阶段 8：绘制提交

把 `InstancedDrawManager.submitDraws()` 的循环翻译成 `RenderPass`：

```java
var encoder = RenderSystem.getDevice().createCommandEncoder();
var pass = encoder.createRenderPass(...);
for (var draw : draws) {
    pass.setPipeline(draw.pipeline());
    pass.setVertexBuffer(0, meshPool.vertexBuffer());
    pass.setIndexBuffer(meshPool.indexBuffer(), IndexType);
    pass.bindTexture("_flw_instanceTex", instanceTexture, sampler);
    pass.setUniform("_flw_packedMaterial", ...);
    pass.drawIndexed(indexCount, instanceCount, indexOffset, baseVertex);
}
pass.close();
```

验收：**Create 的机器第一次出现在屏幕上** ← 这是项目的成败分水岭。

工作量：2–3 天。

---

### 阶段 9：材质状态与深度/混合

`MaterialRenderState.setup(material)` 管深度测试、混合、cull 模式 —— 在 Vulkan 侧对应 `PipelineState`。

验收：不同材质（cutout / translucent）表现正确。

工作量：1–2 天。

---

### 阶段 10：启用与验证

把 `VulkanSupport.probe()` 末尾的 `return false` 改成 `return verdict;`，然后：

- [ ] 功能：所有机械部件渲染、旋转/移动正确
- [ ] 光照：白天/夜晚/方块光源
- [ ] 性能：对比 `flywheel:off` 的 FPS；大量机械（100+ 实例）不崩
- [ ] 稳定性：进出世界 / 换维度 / `/reload` / 连续运行 30 分钟
- [ ] 降级：删掉 VulkanMod 时自动回退到原后端，**游戏不崩**

---

## 五、风险清单

| 风险 | 阶段 | 概率 | 后果 | 应对 |
|---|---|---|---|---|
| **GLSL → SPIR-V 编译失败** | 6 | 中 | 项目受阻 | 早期做最小 shader 验证 |
| **sampler 布局随 oitMode 变化** | 6 | 高 | descriptor set 复杂 | v1 直接砍掉 OIT |
| **GLSL 用 include 但没注册路径** | 6 | 中 | 编译失败 | 阶段 4 就要查清 |
| **依赖 VulkanMod 内部 API**（`net.vulkanmod.vulkan.shader`） | 6 | 中 | VulkanMod 升级即失效 | 用反射 + 优雅降级封装 |
| 性能不如原生 | 10 | 中 | 无意义但能跑 | 阶段 8 就做对比 |
| LightStorage 打包复杂度超预期 | 7 | 中 | 拖时间 | 拆成独立子任务 |

**最大的结构性风险**：我们深度依赖 `net.vulkanmod.vulkan.shader` 这个第三方 mod 的内部包。它是 `public` 但不是稳定 API。建议写一层薄封装（反射或 Mixin accessor），失败时干净降级，绝不让 Flywheel 因 VulkanMod 升级而崩溃。

---

## 六、工作量汇总

| 阶段 | 内容 | 估算 |
|---|---|---|
| 0 | 构建环境 | ✅ 完成 |
| 3 | 注册链路 | ✅ 完成 |
| 4 | GL 依赖盘点 | 0.5 天 |
| 5 | 缓冲层 | 2–3 天 |
| 6 | **Shader 编译** | 3–5 天 |
| 7 | Uniform / 光照 | 2 天 |
| 8 | **绘制提交** | 2–3 天 |
| 9 | 材质状态 | 1–2 天 |
| 10 | 验证收尾 | 1–2 天 |
| | **合计** | **约 12–18 个工作日** |

关键路径是**阶段 6 → 阶段 8**。阶段 4 做完就能把风险进一步收敛。

---

## 七、每轮迭代的固定流程

```
1. 改 F:\Flywheel\src\client\java\com\zurrtum\create\client\flywheel\**
2. cd F:\Flywheel
3. .\deploy.ps1 -DryRun      （先看会替换什么）
4. .\deploy.ps1              （构建 + 备份 + 替换 + 校验）
5. 启动游戏，看日志 [FW/VK]
6. 不满意 → .\deploy.ps1 -Rollback
```

每轮约 1.5 分钟构建 + 游戏里跑几分钟。迭代成本已经压到很低。
# 阶段 4 —— 依赖面盘点

工作区：`F:\Flywheel`，分支 `release` (= `1.21.11_obfuscation`)

---

## 一、结论先行

| 问题 | 答案 |
|---|---|
| GLSL 拼接用 `#include` 吗？ | **用**，但 `SourceFile.parse()` 在 Java 侧**递归内联展开**，最终交给编译器的是**扁平完整 GLSL** |
| 需要 `SPIRVUtils.addIncludePath()` 吗？ | **不需要**（留着无害） |
| GLSL 生成器要改吗？ | **基本不用**。`glsl/` 整包（34 文件）可原样复用 |
| 真正的替换点在哪？ | `gl/shader/`（3 文件）+ `gl/` 其余（18 文件）+ `engine/uniform/`（9 文件） |

---

## 二、`backend/glsl/` —— GLSL 生成层（34 文件）**全部复用**

```
glsl/            8   SourceLines / SourceFile / SourceComponent / ShaderSources / Import / GlslVersion / ...
error/           3   加载与解析错误
lines/           8   带行列号的源文本模型
generate/       12   代码生成（GlslExpr / FnSignature / ...）
span/            3   源码片段定位
```

关键类与结论：

| 类 | 作用 | 结论 |
|---|---|---|
| `SourceFile` | 解析 `#include` 并递归内联 | **复用**，产出已是扁平 GLSL |
| `Import` | 正则 `^\s*#\s*include\s+"(.*)"` 扫描 | 复用 |
| `ShaderSources` | 从资源加载 GLSL | 复用 |
| `SourceComponent` / `generate/*` | 代码生成器 | 复用 |
| `GlslVersion` | GLSL 版本管理（V300/V400…） | **需确认 shaderc 支持的版本** |

**唯一的潜在改动点**：Flywheel 会根据 `GlCompat.MAX_GLSL_VERSION` 和 `requireExtensions(extensions)` 生成不同方言。shaderc 对 `#version` 和扩展的支持需要逐个核对。

---

## 三、`backend/compile/` —— 编译编排（26 文件）**结构复用，产物类型替换**

```
compile/     11   PipelineCompiler / InstancingPrograms / IndirectPrograms / ContextShader / OitMode
component/    6   InstanceStructComponent / UberShaderComponent
core/         9   CompilationHarness / Compile / ShaderCache / ProgramLinker / LinkResult / ShaderResult
```

**关键接缝** —— `CompilationHarness.KeyCompiler`：

```java
public interface KeyCompiler<K> {
    GlProgram compile(K key, ShaderSources loader, ShaderCache cache, ProgramLinker linker);
}
```

这是「GLSL 文本 → 编译后程序」的唯一抽象点。**只要把返回类型 `GlProgram` 换成 `VkProgram`，整条链就能复用。**

| 类 | 结论 |
|---|---|
| `Compile` / `CompilationHarness` / `ShaderCache` | **复用**（缓存、去重、生命周期都通用） |
| `ProgramLinker` / `LinkResult` | **需重写**（GL 的 link 状态机 → Vulkan 的 pipeline 创建） |
| `PipelineCompiler` | 改返回类型 + 改 `postLink` 里的绑定设置 |
| `InstancingPrograms` / `IndirectPrograms` | 改用新编译路径 |
| `PipelineProgramKey`（7 维缓存键） | **复用**，决定了 shader 组合规模 |

---

## 四、`backend/gl/` —— OpenGL 绑定层（21 文件）**这是主要替换面**

```
gl/            9   GlCompat / GlStateTracker / TextureBuffer / GlObject / Driver / GlTextureUnit / GlFence / GlPrimitive / GlNumericType
gl/array/      5   GlVertexArray (+DSA / GL3 / SeparateAttributes) / VertexAttribute
gl/buffer/     4   Buffer / GlBuffer / GlBufferType / GlBufferUsage
gl/shader/     3   GlProgram / GlShader / ShaderType
```

### 替换清单

| 类 | 职责 | Vulkan 对应 | 难度 |
|---|---|---|---|
| **`gl/shader/GlProgram`** | 编译/链接/绑定/uniform 设置 | **`Pipeline.Builder` + `GraphicsPipeline`** | ★★★ |
| `gl/shader/GlShader` | 单个 GL shader | `SPIRVUtils.compileShader` | ★★ |
| `gl/GlCompat` | 能力探测（就是它探测失败导致后端禁用） | `GpuDevice.getBackendName()` 等 | ★ |
| `gl/TextureBuffer` | **实例数据存纹理** | `GpuTexture` + `GpuTextureView` | ★★ |
| `gl/array/GlVertexArray` | VAO | `GpuBuffer` + `RenderPass.setVertexBuffer` | ★★ |
| `gl/buffer/GlBuffer` | VBO | `GpuBuffer` | ★★ |
| `gl/GlStateTracker` | GL 状态快照/还原 | **可删除**（Vulkan 无全局状态） | ★ |
| `gl/GlObject` | GL 对象生命周期基类 | 可删除 | ★ |
| `gl/GlFence` | 同步 | `GpuDevice.createFence()` | ★★ |
| `gl/GlTextureUnit` | sampler 槽位枚举 | 改为 descriptor set/binding | ★★ |
| `gl/Driver` | 厂商识别 | 保留（优先级判断用） | — |
| `gl/GlPrimitive` / `GlNumericType` | 枚举 | 保留 | — |

### `GlProgram` 需要复刻的接口

```java
bind() / unbind()
setFloat / setVec2 / setVec3 / setVec4 / setMat4 / setMat3 / setBool / setUInt / setUVec2 / setInt
getUniformLocation(String)
setSamplerBinding(String, GlTextureUnit)
setUniformBlockBinding(String, int)
bindAttribLocation(String, int)
```

Vulkan 侧对应关系：

| GlProgram 方法 | Vulkan 对应 |
|---|---|
| `bindAttribLocation(name, 0..5)` | `Pipeline.Builder(VertexFormat)` 已封装 |
| `setSamplerBinding(name, unit)` | `addImageDescriptor(ImageDescriptor)` |
| `setUniformBlockBinding(name, binding)` | `addUBO(UBO)` |
| `setFloat/setVec4/setUInt/...` | `RenderPass.setUniform(name, GpuBufferSlice)` |
| `bind()` / `unbind()` | 无对应（Vulkan 无全局状态） |

---

## 五、`backend/engine/uniform/` —— Uniform 层（9 文件）**需重写**

```
DebugMode / FogUniforms / FrameUniforms / LevelUniforms / OptionsUniforms
PlayerUniforms / UniformBuffer / Uniforms / UniformWriter
```

GL 的做法是「每个 uniform 一条 `glUniform*` 调用」，Vulkan 要求**打包成 uniform buffer**。这是**结构性改动**，不是简单替换。

已知要绑的 sampler（来自 `PipelineCompiler.postLink`）：

```
flw_diffuseTex / flw_overlayTex / flw_lightTex
_flw_depthRange / _flw_coefficients / _flw_blueNoise
```

**注意**：`_flw_depthRange` 和 `_flw_coefficients` 只在 OIT 模式下存在 → **descriptor set 布局随 `oitMode` 变化**。v1 砍掉 OIT 可规避。

---

## 六、`backend/engine/` 其余关键类

| 类 | 结论 |
|---|---|
| `EngineImpl` | 去掉 `GlStateTracker.getRestoreState()` 包装即可 |
| `DrawManager` | 新增 `meshPool()` 已就位，接口可复用 |
| `InstancedDrawManager` | **主要参考实现**，提交逻辑要翻译成 `RenderPass` |
| `IndirectDrawManager` | 同上，v1 不做 |
| `MaterialRenderState` | 深度/混合状态 → `PipelineState` |
| `LightStorage` | 光照数据打包成 UBO |
| `EnvironmentStorage` / `Uniforms` | 改走 GpuBuffer |

---

## 七、OIT 建议整体延后

`OitFramebuffer` 的五连 pass 是独立子系统：

```
prepare → depthRange → DEPTH_RANGE pass → renderTransmittance
→ GENERATE_COEFFICIENTS pass → renderDepthFromTransmittance
→ accumulate → EVALUATE pass → composite
```

v1 跳过，半透明部件降级为普通渲染。

---

## 八、三方工作拆分建议

按**文件所有权互不重叠**划分，可并行：

| Agent | 负责目录 | 交付物 |
|---|---|---|
| **A · GLSL 源码层** | `backend/glsl/**`（只加不改） | `VkGLSLAssembler`：由 `PipelineProgramKey` 产出最终扁平 GLSL 字符串；核对 `#version` / 扩展 |
| **B · 编译与管线层** | 新建 `backend/vulkan/shader/**` | `VkProgram`：对接 `Pipeline.Builder` / `SPIRVUtils`，实现 `GlProgram` 的同等表面 |
| **C · Uniform / Descriptor 层** | 新建 `backend/vulkan/uniform/**` | `VkUniformBuffer` / descriptor set 布局，`GlProgram` 那些 `setXxx` 的 Vulkan 实现 |

**预留目录**：`backend/vulkan/**` 下 A/B/C 各建自己的子包，`Backends.java` 与 `VulkanDrawManager.java` 由主控维护，agent 不要动。
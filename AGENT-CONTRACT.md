# 三个 Agent 协作契约

工作区：`F:\Flywheel`（分支 `release`）—— **共用同一份，不隔离**

---

## 目录所有权（严格互斥，不得越界）

| Agent | 唯一可写目录 | 产出 |
|---|---|---|
| **A** | `src/client/java/com/zurrtum/create/client/flywheel/backend/glsl/**` | `VkGLSLAssembler` |
| **B** | `src/client/java/com/zurrtum/create/client/flywheel/backend/vulkan/shader/**` | `VkProgram` |
| **C** | `src/client/java/com/zurrtum/create/com/zurrtum/.../backend/vulkan/uniform/**` | `VkUniformBuffer` + `VkDescriptorLayout` |

**所有人一律不得触碰：**

```
backend/gl/**                    ← 原实现，只读参考
backend/compile/**               ← 原实现，只读参考
backend/engine/**                ← 原实现，只读参考
backend/Backends.java            ← 我维护
backend/vulkan/VulkanDrawManager.java
backend/vulkan/VulkanSupport.java
deploy.ps1 / gradlew.bat / build.gradle / gradle.properties
resources/**                     ← 本轮不动 shader 资源
```

## 禁止的操作

```
✗ 不要运行 .\gradlew.bat 任何命令（构建权在主控）
✗ 不要运行 deploy.ps1（会改动用户的游戏文件）
✗ 不要执行 git add / commit / checkout / stash
✗ 不要修改自己目录以外的任何文件
✗ 不要删除自己没创建的文件
✓ 可以自由读取任何文件
```

## 因为不能自己编译，必须遵守

1. **不要猜方法签名** —— 任何要调用的类，先打开源文件确认签名
2. **照抄现有类的 import 顺序和风格**，减少格式问题
3. 拿不准 API 形状时，参考 `backend/engine/instancing/InstancedDrawManager.java` 的写法
4. 写完后自己逐行读一遍，检查 import 是否齐全、泛型参数是否匹配

## 主控的职责

- 三个 agent 汇报完成后，我统一执行 `.\gradlew.bat compileClientJava`
- 编译错误我负责分发回对应 agent
- 编译通过后再部署测试

---

## 已确认的关键事实（省去重复调研）

| 事实 | 影响 |
|---|---|
| GLSL 的 `#include` 由 `SourceFile.parse()` 在 Java 侧**递归内联展开**，最终是扁平文本 | 不需要 `SPIRVUtils.addIncludePath()` |
| uniform block 已声明为 `layout(std140)` | 与 Vulkan UBO 布局一致，可直译 |
| **instancing 后端只要 3 个 shader**：`light.glsl` / `main.frag` / `main.vert` | v1 不碰 indirect 的 11 个 compute shader |
| compute shader 全在 indirect 后端（`cull` / `scatter` / `apply` / `downsample`） | v1 完全绕开 |
| 资源里没有 `#version`，由 `GlCompat.MAX_GLSL_VERSION` 注入 | 版本由编译期决定 |
| 参考实现：`F:\Beryl\src` 有 45 个 Java 文件，`SUtil.createGraphicsPipeline` 是 VulkanMod `Pipeline.Builder` 的现成用法 | B/C 直接照抄 |
| Beryl 的 `ImageDescriptorMixin` 只是条件透传 | 不用移走 Beryl |
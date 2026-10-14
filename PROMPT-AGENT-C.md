工作区：F:\Flywheel（分支 release，三个 agent 共用）

═══════════════════════════════════════════════
一、背景
═══════════════════════════════════════════════
Flywheel 是 Create 的渲染引擎，用 OpenGL 的 glUniform* 逐个设置 uniform。
在 VulkanMod 下不可用。要改成 Vulkan 的 uniform buffer（UBO）+ descriptor。

Agent B 已完成 shader 编译与管线层，定义了接口 VkUniformAccess。
你的任务是实现它的实现类。

═══════════════════════════════════════════════
二、工作规则
═══════════════════════════════════════════════
✓ 可自由读 F:\ 盘任何文件
✗ 不要运行 gradlew / deploy.ps1
✗ 不要执行 git add / commit / checkout
✗ 不要修改以下目录：
    backend/glsl/**、backend/vulkan/shader/**、
    backend/compile/**、backend/engine/**
✗ 不要改 Backends.java / VulkanDrawManager.java

不能自己编译：打开源文件确认签名再写；VulkanMod / MC 的类用 javap 看。
写完自己逐行读，检查 import 和泛型。

先读 F:\Flywheel\AGENT-CONTRACT.md

═══════════════════════════════════════════════
三、你的目录所有权
═══════════════════════════════════════════════
唯一可写：
  src/client/java/com/zurrtum/create/client/flywheel/backend/vulkan/uniform/**

═══════════════════════════════════════════════
四、⚠ 最重要：照抄 Beryl 的做法
═══════════════════════════════════════════════
Beryl 是在 VulkanMod 上已经跑通的光影模组，源码在 F:\Beryl\src（原始源码，
不是反编译产物，零反编译痕迹）。

**不要自己设计 uniform 方案，照 Beryl 的写法来。**
这样既省事，又保证和 Beryl 不冲突。

【Beryl 的 GLSL 写法】
  F:\Beryl\src\assets\beryl\shaders\blit\blit.vsh
  F:\Beryl\src\assets\beryl\shaders\terrain\terrain.vsh

    #version 450

    layout(location = 0) in vec3 Position;
    layout(location = 1) in vec2 UV0;

    layout(binding = 0) uniform UniformBufferObject {
        mat4 MVP;
    };

    layout(location = 0) out vec2 texCoord0;

  → UBO 是「匿名 block + 显式 layout(binding = N)」
  → sampler 也是显式 layout(binding = N)：
      layout(binding = 1) uniform sampler2D Sampler0;

【Beryl 的 Java 侧用法】
  F:\Beryl\src\net\beryl\render\util\SUtil.java
  F:\Beryl\src\net\beryl\render\shader\BerylPipelineConfigs.java
  F:\Beryl\src\net\beryl\render\RenderingPipeline.java   （41KB，45 处引用 VulkanMod API）

  → PipelineConfig.builder() 流式 API
  → UBO / ImageDescriptor 怎么构造、怎么传给 Pipeline.Builder

请先把这几个文件读一遍，照着写。

═══════════════════════════════════════════════
五、Agent B 已完成的接口（你要实现）
═══════════════════════════════════════════════
读 F:\Flywheel\src\client\java\com\zurrtum\create\client\flywheel\
    backend\vulkan\shader\VkUniformAccess.java
  → 这是 B 定义的契约，你的类要实现它

读 VkBindings.java
  → 描述符 binding 与 stage 的唯一真源表
  → 已固定：6 个 UBO 占 binding 0..5，7 个 sampler 占 6..12
  → VulkanMod 的 Pipeline.createDescriptorSetLayout() 用
    bindings.get(bindingNumber) 直接当数组下标，所以必须 0..N-1 连续无空洞
  → B 已经处理好了，你不要改这个表，只消费它

读 VkProgram.java 的 Builder 部分
  → 看你产出的对象是怎么被消费的
  → addUBO(UBO) / addImageDescriptor(ImageDescriptor) 要拿到什么

═══════════════════════════════════════════════
六、任务
═══════════════════════════════════════════════
1. VkUniformBuffer
   实现 VkUniformAccess 接口：setFloat / setVec2 / setVec3 / setVec4 /
   setMat4 / setMat3 / setInt / setUInt / setBool / setUVec2

   OpenGL 是「每次调用立刻发一条 glUniform*」，Vulkan 要求打包成 buffer。
   所以这里要维护每帧的 CPU 端数据，帧末一次性上传成 GpuBufferSlice。

2. VkDescriptorLayout
   描述这个 shader 需要哪些 UBO 和 sampler，
   产出 VulkanMod 的 net.vulkanmod.vulkan.shader.descriptor.UBO
   和 ImageDescriptor 对象，交给 B 的 Pipeline.Builder。

3. 如果需要接口类或常量类，也在你的目录里建。

═══════════════════════════════════════════════
七、必须先回答的问题
═══════════════════════════════════════════════
1. GpuBufferSlice 怎么造？怎么写入数据？
   看 MC 的 com.mojang.blaze3d.buffers.GpuBuffer / GpuBufferSlice，
   以及 CommandEncoder.mapBuffer / writeToBuffer。
   用 javap 反编译看。

2. GpuDevice.getMaxUniformOffsetAlignment() 返回什么？
   std140 的对齐规则要不要额外处理？

3. Beryl 怎么上传 UBO 数据的？
   读 F:\Beryl\src 里 setUniformSupplierGetter / MappedBuffer 相关代码。
   照抄。

4. 6 个 UBO 分别对应什么？
   看 VkBindings.java 的定义，以及 Flywheel 的 GLSL：
     resources/assets/flywheel/flywheel/internal/
       frame.glsl / level.glsl / player.glsl / fog.glsl / options.glsl
     每个文件里的 layout(std140) uniform _FlwXxxUniforms { ... }
   → 请给出一张对照表：UBO binding 号 ↔ GLSL block 名 ↔ Java 侧数据来源

5. 7 个 sampler 分别是什么？对应 VkBindings 里的哪几个 slot？
   来自 VkBindings 和 PipelineCompiler.postLink 的 setSamplerBinding：
     flw_diffuseTex / flw_overlayTex / flw_lightTex
     _flw_depthRange / _flw_coefficients / _flw_blueNoise
   注意：我们 v1 不做 OIT，后两个用不到，实际只用前 5 个左右。
   请确认实际需要几个。

6. FrameUniforms / LevelUniforms / PlayerUniforms / FogUniforms /
   OptionsUniforms 每个字段对应 GLSL 里的哪个成员？
   给出完整对照表，含 std140 偏移量。

7. 哪些 uniform 每帧都变？哪些很少变？
   （影响要不要每帧重建 buffer，还是只更新部分）

8. 单个 UBO 会不会超 16KB？超了要拆分吗？
   先算总大小再说。

═══════════════════════════════════════════════
八、已确认的事实
═══════════════════════════════════════════════
✦ GLSL 里的 uniform block 已经是 layout(std140)，与 Vulkan UBO 布局一致。
  CPU 端打包必须严格遵守 std140 规则：
  vec3 按 16 字节对齐、数组元素按 16 字节对齐、mat3/mat4 特殊规则。
✦ B 已经把裸 uniform 打包进 binding=5 的 _FlwDrawUniforms（std140, 128 字节），
  你不需要再处理裸 uniform。
✦ B 的 VkShaderSource 会给所有裸 in/out 补 layout(location = N)，
  flat 限定符保留。你不需要处理顶点输入。
✦ Beryl 的 vkmod/mixin/vkmod/texture/ImageDescriptorMixin 对
  ImageDescriptor.getImageView 做了 @Redirect，但只是带条件透传
  （只有 Beryl 的 sRGB 选项开启且纹理格式为 37 时才改变行为），
  所以不影响我们。Beryl 保持安装。
✦ binding 编号必须 0..N-1 连续无空洞 —— VulkanMod 的硬要求，
  VkBindings 已经排好了（UBO 0-5，sampler 6-12）。
✦ OIT v1 不做，相关 sampler 不要纳入。

═══════════════════════════════════════════════
九、报告格式
═══════════════════════════════════════════════
需要我决策的：

  问题：一句话说清
  查到：你的发现
  需要：你要我决定什么（或者"我自己定了：XXX"）

实现细节能自己定就自己定，报告里提一句。不要因小问题停下来问。

报告里必须包含第七部分的对照表 —— 那是 B 和我后续接线都要用的。

特别提醒：如果你发现 UBO 装不下、std140 对齐过于复杂、
或者 VulkanMod 的 UBO API 和预期差很多，立刻报告，这会影响整体设计。

最后给完整报告，我负责编译验证。

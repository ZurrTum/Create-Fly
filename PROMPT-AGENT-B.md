工作区：F:\Flywheel（分支 release，三个 agent 共用）

═══════════════════════════════════════════════
一、背景
═══════════════════════════════════════════════
Flywheel 是 Create 的渲染引擎，用 OpenGL 编译运行时生成的 GLSL。
在 VulkanMod 下探测 GL 能力失败 → 后端整个被禁用。

目标：保留 GLSL 生成器，只把「编译」这一步从 OpenGL 换成 Vulkan。

上游 Agent A 的调研结论已由主控逐条复核，全部属实。
第七部分是复核后的完整结论，直接用，不用重做调研。

═══════════════════════════════════════════════
二、工作规则
═══════════════════════════════════════════════
✓ 可自由读 F:\ 盘任何文件
✗ 不要运行 gradlew（任何形式）
✗ 不要运行 deploy.ps1
✗ 不要执行 git add / commit / checkout
✗ 不要修改以下目录（别人的地盘）：
    backend/glsl/**
    backend/vulkan/uniform/**
    backend/compile/**
    backend/engine/**
✗ 不要改 Backends.java / VulkanDrawManager.java

不能自己编译，所以：
1. 不猜签名。打开源文件确认再写；VulkanMod / MC 的类在 jar 里，
   用 javap 反编译查看
2. 写完自己逐行读，检查 import 和泛型参数
3. 拿不准就查

先读 F:\Flywheel\AGENT-CONTRACT.md

═══════════════════════════════════════════════
三、你的目录所有权
═══════════════════════════════════════════════
唯一可写：
  src/client/java/com/zurrtum/create/client/flywheel/backend/vulkan/shader/**

另外两个 agent 同时在写：
  Agent A → backend/glsl/**（在写 VkGLSLAssembler）
  Agent C → backend/vulkan/uniform/**
绝对不要碰。

═══════════════════════════════════════════════
四、⚠ 第一件事：一个必须先回答的门槛问题
═══════════════════════════════════════════════
动手写任何代码之前，先回答并立刻单独报告：

  VulkanMod 支不支持 VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER？

为什么重要：Flywheel 的 instancing 路径用了 GL texture buffer：
    instancing/light.glsl:3-4
      uniform usamplerBuffer _flw_lightLut;
      uniform usamplerBuffer _flw_lightSections;
    BufferTextureInstanceComponent.java:43
      uniform usamplerBuffer _flw_instances;
编译到 SPIR-V 是 OpTypeImage...Buffer + UniformConstant
→ VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER。

主控已排除的查法（别重复）：
  ✗ 搜字符串 —— VulkanMod 用内联整数常量，整个 jar 里
    TEXEL_BUFFER 零命中，也没有 VK_DESCRIPTOR_TYPE_* 符号
  ✗ 看别人用没用 —— VulkanMod 自带 17 个 shader、
    Beryl 全部 shader，都是 0 个 samplerBuffer。
    但这不能证明不支持，只是没有正面证据
  ✗ Descriptor 是接口，UBO.getType() 返回 8，
    不是 Vulkan 规范的 6，编号体系不透明

建议的查法（按可行性排序）：
  1. javap -c 看 net/vulkanmod/vulkan/shader/descriptor/ 下所有类。
     ImageDescriptor 有 isStorageImage()，看它的 type 取值范围，
     有没有第4/第5 种 texel buffer 类型
  2. 看 net/vulkanmod/vulkan/memory/buffer/ 下有没有
     texel / staging buffer 的支持代码
  3. 翻 F:\Beryl\src，RenderingPipeline.java（41KB）
     有没有处理 buffer 类型描述符

如果静态分析得不出结论，就直接说「需要真机验证」，
我会安排最小验证实验。不要靠猜，不要编。

【两种结果分别怎么走】
  支持 → 按原计划做。
  不支持 / 不确定 → 停下报告，不要自己改。
    改法是把这 3 个 usamplerBuffer 改写成 SSBO：
      layout(std430, binding=N) readonly buffer X { uint data[]; } _flw_instances;
      texelFetch(_flw_instances, i, 0).r  →  _flw_instances.data[i]
    GLSL 层面机械替换。Flywheel 的 cull.glsl 本来就在用
    std430 buffer，所以语法它会写。

═══════════════════════════════════════════════
五、你的核心任务
═══════════════════════════════════════════════
实现 VkProgram —— 取代 backend/gl/shader/GlProgram.java 的 Vulkan 版本，
**并在自己的目录里复刻编译链**（原因见第七部分）。

需要提供等价能力的 GlProgram 方法（打开该文件自己列全）：
  bind() / unbind()
  setFloat / setVec2 / setVec3 / setVec4 / setMat4 / setMat3
  setBool / setUInt / setInt / setUVec2
  getUniformLocation(String)
  setSamplerBinding(String, GlTextureUnit)
  setUniformBlockBinding(String, int)
  bindAttribLocation(String, int)

对应关系：
  bind()/unbind()           → Vulkan 无全局状态，可空实现
  bindAttribLocation(0..5)  → 由 Builder(VertexFormat) 封装
  setSamplerBinding         → addImageDescriptor(ImageDescriptor)
  setUniformBlockBinding    → addUBO(UBO)
  setXxx 系列               → 交给 Agent C 的 uniform 层
  getUniformLocation        → Vulkan 无此概念

你在自己的目录里定义接口，Agent C 依赖你的接口。
不要去写 Agent C 的目录。

═══════════════════════════════════════════════
六、Agent A 会提供的接口
═══════════════════════════════════════════════
package com.zurrtum.create.client.flywheel.backend.glsl;

public final class VkGLSLAssembler {
    public static VkAssembly assemble(String glslSource, String stage);
    // stage ∈ {"vertex","fragment","compute"}
    // 故意用 String 而非 ShaderType —— 那个类带 lwjgl GL 常量
}
public record VkAssembly(
    String source,                  // 改写后的 GLSL
    List<String> droppedExtensions, // 被删的 extension（进日志）
    List<String> droppedDefines     // 被删的 define（进日志）
) {}

你负责拼装 fullSource → 调 A 的 assembler → 喂给 Pipeline.Builder。
每个被删的宏都意味着一次功能退化，A 会列出来，你负责打进日志。

A 可能还没写完，按这个接口设计，不要等。

═══════════════════════════════════════════════
七、已复核的调研结论（直接用）
═══════════════════════════════════════════════

【1. glsl 层可以整层保留】
backend/glsl/ 34 个文件，零 OpenGL 引用。
完整 import 清单只有：Guava / datafixers / fastutil / jetbrains /
MC 的 Identifier+ResourceManager / glsl 包自身 / FlwPrograms(日志) / StringUtil / ResourceUtil。
你的复刻可以直接复用它的全部产物。

【2. GLSL 文本在哪】
backend/compile/core/Compilation.java
  L30:  private final StringBuilder fullSource;      ← 最终文本
  L29:  private final StringBuilder generatedSource;  ← 仅 Java 侧生成部分
  L28:  private final List<SourceFile> files;
  L31:  private int generatedLines;
public 方法只有：compile / version / enableExtension / requireExtension
                 / define x2 / appendComponent / compiledSuccessfully
→ 没有任何 getter。

唯一读取点 Compilation#compile(ShaderType, String) L33-57：
  int handle = GL20.glCreateShader(shaderType.glEnum);
  var source = fullSource.toString();          ← 文本在这里被读走
  GlCompat.safeShaderSource(handle, source);
  GL20.glCompileShader(handle);
  var shaderName = name + "." + shaderType.extension;
  dumpSource(source, shaderName);
  ...
★ 注意 dumpSource(source, shaderName) 每次 compile 都会调，
  源码字符串在那一刻是可达的 —— 你的复刻版本也应该做这个 dump。

【3. 编译链是封闭的 —— 这就是必须复刻的原因】
PipelineCompiler.create()                [compile/** 不可写]
 └─ Compile.ProgramStitcher#compile      返回 GlProgram ← 硬编码
     └─ ShaderCompiler#compile          private，返回 GlShader
         └─ ShaderCache#compile         Map<ShaderKey, ShaderResult> ← GL 句柄
             └─ new Compilation()
             └─ Compilation#compile     ← 文本在此被读走
ShaderCache 和 ProgramLinker 在 CompilationHarness 构造函数里 new 死，
Compile.ShaderCompiler#compile 是 private。
整条链返回类型写死 GlProgram/GlShader，没有任何注入点。

【4. 可复用的 public 组件（可见性已验证）】
  SourceComponent#source() / #name() / #included()      public ✓
  SourceFile#finalSource                                public final String ✓
                                                     （主控补充：字段本身就是 public，
                                                      比 getter 还直接）
  ShaderSources#get(Identifier) / get(...)               public ✓
  InstanceStructComponent / BufferTextureInstanceComponent
    / InstanceAssemblerComponent            全部 public ✓
  UberShaderComponent.builder(Identifier)      public static ✓
  GlslBuilder / GlslExpr / FnSignature / ...   public ✓
  Compilation#version() / define() / enableExtension()
    / requireExtension() / appendComponent()    全部 public ✓

【5. include 展开机制（你要复刻这部分）】
不是就地内联，是分两步：
  1. SourceFile.generateFinalSource(imports, source)  [L100]
     把 #include 那一行挖掉，finalSource 只剩本文件正文
  2. ShaderCache.expand(List<SourceComponent>, Consumer)  [L49]
     + recursiveDepthFirstInclude(included, component)  [L58]
     LinkedHashSet 去重保序，DFS 收集 SourceComponent#included()，
     逐个交给 Compilation#appendComponent
结果：最终文本是各文件的拼接。
复核数据：81 个 shader 文件 / 63 处 include / 0 处尖括号形式。
资源里 #version 出现 0 次（Flywheel 路径），#extension 出现 0 次。

【6. 需要复刻的源码，照着抄】
  backend/compile/core/Compilation.java:33-104   文本组装循环（~40 行）
  backend/compile/core/ShaderCache.java:49-63     DFS 展开（~15 行）
  backend/compile/PipelineCompiler.java:84-179    builder 接线（~60 行）

【7. GLSL 需要做的 3 处改写】
A 的 VkGLSLAssembler 会帮你做，但你必须知道原因：

① #version 150 → 450
   Compilation#version() L60: fullSource.append("#version ").append(version.version)
   传入的是 GlCompat.MAX_GLSL_VERSION（L39 = maxGlslVersion()）
   而 maxGlslVersion()：CAPABILITIES == null 时直接 return GlslVersion.V150
   V150 下 texelFetch(usamplerBuffer,…) / uint[27] / in-out 全编译不过。

② 删 #extension GL_ARB_conservative_depth : enable
   注入点：PipelineCompiler.java:129（fragment 链路，无条件）
   它会激活 common.frag:8-9：
     #if defined(GL_ARB_conservative_depth) && defined(_FLW_USE_DISCARD)
       layout (depth_greater) out float gl_FragDepth;
   Vulkan 硬错误：gl_FragDepth 是语言保留字，不能被重新声明为输出变量。
   删掉 #extension 后 defined() 为假，整个 #if 块跳过。
   代价：丢 early-Z 优化，只影响性能，不影响画面。v1 可接受。

③ 删 #extension GL_ARB_shader_bit_encoding 和 #define fma(a,b,c)
   触发条件：PipelineCompiler.java:102/131
     MAX_GLSL_VERSION.compareTo(V400) < 0 → 必然成立（因为是 V150）
   - shader_bit_encoding 是死代码：packed_material.glsl 全是手写 >> 和 &，
     没用到 bitfield 内建
   - fma 宏会遮蔽 GLSL 4.50 的内建 fma()。而且 wavelet.glsl 真的在调 fma()：
     L25, L31, L128, L151（wavelet.glsl 被 common.frag include，在 instancing 路径上）
   已决定：删。让 450 的内建 fma() 接管，精度更好。

【8. GLSL 方言核查结果 —— 已经很干净】
全资源零命中：texture2D / texture3D / gl_FragColor / gl_FragData
              / varying / attribute
已经是现代核心 GLSL（in/out + texture() + 显式 out）。
fragment 输出是显式 out vec4 _flw_outputColor（common.frag:69）。
OIT 分支的 _flw_coeffs0..3 / _flw_accumulate / _flw_depthRange_out
也都是显式 out，Vulkan 合法（只是 v1 不启用）。

【9. OIT：v1 永远关掉】
instancing/main.vert 只有 FLW_EMBEDDED 分支（3 个 uniform）
instancing/main.frag 零 #ifdef，就 3 行 unpack + 调 _flw_main()
OIT 全在它们 include 的 common.frag 里（L22/46/52/61/155-209），
由 _FLW_OIT / _FLW_DEPTH_RANGE / _FLW_COLLECT_COEFFS / _FLW_EVALUATE 控制。
你的 PipelineProgramKey 里 oit 一律传 OitMode.OFF，
common.frag 就走 #else 分支 —— 单一 out vec4 _flw_outputColor。
那条路径是当前不透明方块天天在跑的，不是冷门代码。

【10. 参考实现：F:\Beryl\src】
Beryl 是在 VulkanMod 上跑通的 shader 模组，45 个 Java 文件。
net\beryl\render\util\SUtil.java 的 createGraphicsPipeline()：

  Builder b = new Builder(format, path);
  b.applyConfig(pipelineConfig);
  b.setShaderSrc(ShaderKind.VERTEX_SHADER,   <GLSL>);
  b.setShaderSrc(ShaderKind.FRAGMENT_SHADER, <GLSL>);
  return b.createGraphicsPipeline();

Beryl 从资源文件读 GLSL，你从内存字符串拿，除此之外流程一致。
务必先读这个文件照着写。

═══════════════════════════════════════════════
八、还要回答的问题
═══════════════════════════════════════════════
1. net.vulkanmod.vulkan.shader.Pipeline$Builder 能直接用吗？
   有静态初始化顺序要求吗？（javap -c 看 <clinit>）
2. GraphicsPipeline.getHandle(PipelineState) 需要什么 PipelineState？
   从哪获取？可复用还是每次新建？
3. PipelineConfig 没有 json 怎么构造？
   （Beryl 用 PipelineConfig.fromJson(name, jsonObject)，我们要直接构造）
4. ShaderKind 枚举的完整取值？（Beryl 写的是 VERTEX_SHADER / FRAGMENT_SHADER）
5. SPIRVUtils.compileShader(source, name, kind) 三个参数的语义？
   失败抛什么异常？能不能拿到错误信息用于日志？
6. VkProgram 的生命周期：谁创建、谁销毁？
   注意 Flywheel 的 ShaderCache（Map<ShaderKey, ShaderResult>）和
   CompilationHarness#programs（Map<K, GlProgram>）管着，
   你要提供对应接口。
7. 复刻的编译链怎么做缓存？
   注意 ShaderCache 的 key 是 (GlslVersion, ShaderType, name)，
   **不含源码文本，只靠 name 唯一标识**。
   而且它失败也缓存（L39 无条件 inner.put），delete() 只清 Success（L44-45）。
   Vulkan 路径要不要沿用这个语义？建议沿用，保持行为一致。
8. 你复刻的版本也要实现 DUMP_SHADER_SOURCE
   （System.getProperty("flw.dumpShaderSource")），
   把源码 dump 到 .minecraft/flywheel_sources/。
   否则 Vulkan 下调 shader 没有眼睛。

═══════════════════════════════════════════════
九、报告格式
═══════════════════════════════════════════════
需要我决策的：

  问题：一句话说清
  查到：你的发现
  需要：你要我决定什么（或者"我自己定了：XXX"）

实现细节能自己定就自己定，报告里提一句。不要因小问题停下来问。

第四部分的门槛问题，无论结论如何都要立刻单独发一条。

最后给完整报告，我负责编译验证。
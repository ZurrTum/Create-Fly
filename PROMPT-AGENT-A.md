工作区：F:\Flywheel（分支 release，共用）

═══════════════════════════════════════════════════════
【第一部分：背景，30 秒读完】
═══════════════════════════════════════════════════════
Flywheel 是 Create 模组的渲染引擎，用 GPU 实例化加速机械方块渲染。
它原本依赖 OpenGL，在 VulkanMod 环境下探测 GL 能力失败，导致整个后端被禁用
（日志：Failed to get GL capabilities; default Flywheel backends will be disabled）。

我们的目标是给它写一个 Vulkan 后端。

═══════════════════════════════════════════════════════
【第二部分：工作规则，务必遵守】
═══════════════════════════════════════════════════════
✓ 可以自由读取任何文件
✗ 不要运行 gradlew（任何形式都不行）
✗ 不要运行 deploy.ps1
✗ 不要执行 git add / commit / checkout
✗ 不要修改自己目录以外的任何文件
✗ 不要删除自己没创建的文件

因为不能自己编译，你必须遵守：
1. 不要猜方法签名。任何要调用的类，先打开源文件确认签名再写。
2. 照抄现有类的 import 顺序和代码风格。
3. 写完自己逐行读一遍，检查 import 是否齐全、泛型参数是否匹配。
4. 拿不准就查，不要猜。

协作契约全文见 F:\Flywheel\AGENT-CONTRACT.md，请先读一遍。

═══════════════════════════════════════════════════════
【第三部分：你的目录所有权】
═══════════════════════════════════════════════════════
你唯一可以写入的目录：
  src/client/java/com/zurrtum/create/client/flywheel/backend/glsl/**

另外两个 agent 正在同时工作：
  Agent B → backend/vulkan/shader/**
  Agent C → backend/vulkan/uniform/**
绝对不要碰他们的目录。

═══════════════════════════════════════════════════════
【第四部分：任务】
═══════════════════════════════════════════════════════
目标：提供「拿到完整 GLSL 源码字符串」的接口，
让 Agent B 能把它直接喂给 VulkanMod 的 Pipeline.Builder.setShaderSrc()。

你要新增一个类：backend/glsl/VkGLSLAssembler.java

═══════════════════════════════════════════════════════
【第五部分：必须先回答的问题（你的调研产出）】
═══════════════════════════════════════════════════════
1. glsl 包产出的最终 GLSL 是否已经完全内联展开、没有残留 #include？
   提示：看 SourceFile.parse() 和 Import.parseImports()。
   验证方法：找出整个流程中「最终交给 GlProgram 编译」的那个字符串是怎么来的，
   在那个位置打断点逻辑，或者跟踪数据流。

2. 生成的是 GLSL OpenGL 方言还是 Vulkan 方言？
   具体检查：
   - texture() vs texture2D()
   - gl_FragColor vs 自定义输出变量
   - 有没有用 GL 特有的内建变量

3. #version 是怎么确定的？由谁注入？最终值可能是哪些？
   提示：GlCompat.MAX_GLSL_VERSION，PipelineCompiler.create() 里用到。

4. PipelineCompiler.create() 里的 requireExtensions(extensions) 传了哪些扩展？
   这些扩展声明最终出现在生成代码的什么位置？shaderc 能否处理？

5. main.vert / main.frag 里 OIT 相关的 #ifdef 分支（_FLW_OIT、
   _FLW_COLLECT_COEFFS、_FLW_DEPTH_RANGE、_FLW_EVALUATE）分别在做什么？
   如果我们 v1 不做 OIT，这些分支能不能干净地砍掉，还是必须留着？

6. glsl 包里有没有任何地方直接调用了 OpenGL？
   如果有，在哪里？（这决定哪些代码必须替换，哪些可以原样保留）

═══════════════════════════════════════════════════════
【第六部分：已确认的事实，不用重复验证】
═══════════════════════════════════════════════════════
✦ #include 由 SourceFile.parse() 在 Java 侧递归内联展开，
  最终交给编译器的是扁平完整 GLSL。所以不需要 SPIRVUtils.addIncludePath()。
  （但请你用第五部分第 1 题独立确认一遍，因为整个方案依赖这个结论。）

✦ uniform block 已声明为 layout(std140)，与 Vulkan UBO 布局一致。

✦ v1 只做 instancing 后端，只需要 3 个 shader 资源：
     internal/instancing/light.glsl
     internal/instancing/main.vert
     internal/instancing/main.frag
  indirect 后端的 11 个 compute shader 完全不碰。

✦ 资源文件里没有 #version，由编译期注入。

═══════════════════════════════════════════════════════
【第七部分：产出】
═══════════════════════════════════════════════════════
1. backend/glsl/VkGLSLAssembler.java
   提供方法，能从编译流水线里拿到某个 key 对应的完整 GLSL 字符串。
   接口怎么设计你定，但要方便 Agent B 调用。

2. 一份简短的报告，包含：
   - 第五部分 6 个问题的答案
   - glsl 包里所有涉及 OpenGL 的地方清单
   - 你的 VkGLSLAssembler 接口设计说明
   - 任何你认为 Agent B / C 需要注意的坑

注意：现有 34 个文件原则上不要改。如果发现 bug 可以改，但必须在报告里说明改了什么、为什么。

═══════════════════════════════════════════════════════
【第八部分：报告格式】
═══════════════════════════════════════════════════════
遇到需要我决策的问题，用这个格式，简短即可：

  问题：一句话说清是什么
  查到：你的发现
  需要：你需要我决定什么（或者"我自己定了：XXX"）

实现细节能自己定的就自己定，然后在报告里提一句你定了什么。
不要因为小问题停下来问，那样效率太低。

最后给我一份完整报告，我负责编译验证。
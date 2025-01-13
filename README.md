# AndroidR8

Android 代码混淆、压缩工具 R8 使用方式及配置

## D8 dexer 和 R8 shrinker

R8 仓库包含两个工具：
- D8 是一个 dexer，用于将 Java 字节码转换为 DEX 代码。
- R8 是一个 Java 程序压缩和混淆工具，用于将 Java 字节码转换为优化后的 DEX 代码。
D8 是 DX dexer 的替代品，而 R8 是 ProGuard 压缩和混淆工具的替代方案。

## R8 缩减、混淆和优化应用

R8 在编译时为应用进行代码缩减、资源缩减、优化和混淆：

- 代码缩减（Code shrinking）：从应用及其库依赖项中检测并安全地移除不使用的类、字段、方法和属性（这使其成为了一个对于规避 64k 引用限制非常有用的工具）。
- 资源缩减（Resource shrinking）：从封装应用中移除不使用的资源，包括应用库依赖项中不使用的资源。此功能可与代码缩减功能结合使用，这样一来，移除不使用的代码后，也可以安全地移除不再引用的所有资源。
- 混淆（Obfuscation）：缩短类和成员的名称，从而减小 DEX 文件的大小。
- 优化（Optimization）：检查并重写代码，以提升运行时性能并进一步减小应用的 DEX 文件的大小。这可将代码的运行时性能提高最多 30%，显著缩短启动时间和帧时间。

在构建应用的发布版本时，我们可以将 R8 配置为执行上述编译时任务。还可以停用某些任务或通过 ProGuard 规则文件自定义 R8 的行为。事实上，R8 支持所有现有 ProGuard 规则文件，因此在更新 Android Gradle 插件以使用 R8 时，无需更改现有规则。
使用 Android Studio 3.4 或 Android Gradle 插件 3.4.0 及更高版本时，R8 是默认编译器，用于将项目的 Java 字节码转换为在 Android 平台上运行的 DEX 格式。

## 启用缩减、混淆处理和优化功能

模块级别 **build.gradle.kts** 文件 

```kotlin
android {
    buildTypes {
        // 发布环境
        release {
            // 仅启用代码缩减、混淆和优化
            isMinifyEnabled = true
            // 应用是否可调试
            isDebuggable = false
            // 启用资源压缩，由 Android Gradle 插件执行。
            isShrinkResources = true

            proguardFiles(
                // 包括与 Android Gradle 插件一起打包的默认 ProGuard 规则文件
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // 包括本地自定义 Proguard 规则文件
                "proguard-rules.pro"
            )
        }
    }
}
```
为了方便测试 R8 功能的效果，我写了个小功能，代码用于做测试，功能如下图所示：

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/img_01.png?raw=true" alt="首页" style="zoom:25%;" /> <img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/img_02.png?raw=true" alt="弹窗" style="zoom:25%;" />

通过 **gradle task: assembleRelease** 进行一次打包工作，得到一个 apk 文件，我们通过 **jadx** 直接查看该 apk 混淆优化后的源代码，这里我拿 `LocalRepo` 这个类的代码进行对比混淆前后的样子：

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/original_code.png?raw=true" alt="原始代码" style="zoom:25%;" /> <img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/optimized_code.png?raw=true" alt="优化后的代码" style="zoom:25%;" />

从上图中可以看到，优化后的代码包名、类名、变量名等都被修改为无意义字符，并且 `LocalRepo` 这个类的其他代码也被 R8 进行优化插入到其他位置了。这就是开启混淆压缩后代码的模样，并且我们没有做其他的自定义配置，这是一个默认配置加第三方库的内置混淆规则所产生的一个结果。

## 资源

[ProGuard manual](https://www.guardsquare.com/manual/configuration/usage)

[Android Developers 缩减、混淆处理和优化应用](https://developer.android.com/build/shrink-code?hl=zh-cn)
# AndroidR8

Android 代码混淆、压缩工具 R8 使用方式及配置

本文讲解了 Android R8 的功能详细介绍、如何编写混淆规则以及用 demo 进行演示效果。

## 一、D8 dexer 和 R8 shrinker

R8 仓库包含两个工具：
- D8 是一个 dexer，用于将 Java 字节码转换为 DEX 代码。
- R8 是一个 Java 程序压缩和混淆工具，用于将 Java 字节码转换为优化后的 DEX 代码。
  D8 是 DX dexer 的替代品，而 R8 是 ProGuard 压缩和混淆工具的替代方案。

## 二、R8 缩减、混淆和优化应用

R8 在编译时为应用进行代码缩减、资源缩减、优化和混淆：

- 代码缩减（Code shrinking）：从应用及其库依赖项中检测并安全地移除不使用的类、字段、方法和属性（这使其成为了一个对于规避 64k 引用限制非常有用的工具）。
- 资源缩减（Resource shrinking）：从封装应用中移除不使用的资源，包括应用库依赖项中不使用的资源。此功能可与代码缩减功能结合使用，这样一来，移除不使用的代码后，也可以安全地移除不再引用的所有资源。
- 混淆（Obfuscation）：缩短类和成员的名称，从而减小 DEX 文件的大小。
- 优化（Optimization）：检查并重写代码，以提升运行时性能并进一步减小应用的 DEX 文件的大小。这可将代码的运行时性能提高最多 30%，显著缩短启动时间和帧时间。

在构建应用的发布版本时，我们可以将 R8 配置为执行上述编译时任务。还可以停用某些任务或通过 ProGuard 规则文件自定义 R8 的行为。事实上，R8 支持所有现有 ProGuard 规则文件，因此在更新 Android Gradle 插件以使用 R8 时，无需更改现有规则。
使用 Android Studio 3.4 或 Android Gradle 插件 3.4.0 及更高版本时，R8 是默认编译器，用于将项目的 Java 字节码转换为在 Android 平台上运行的 DEX 格式。

### 启用缩减、混淆处理和优化功能

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

从上图中可以看到，优化后的代码包名、类名、变量名等都被修改为无意义字符，并且 `LocalRepo` 这个类的其他代码也被 R8 进行优化插入到其他位置了。这就是开启混淆压缩后代码的模样，并且我们暂时没有做其他的自定义配置。

### R8 配置文件

R8 使用 ProGuard 规则文件来修改其默认行为，并更好地理解应用程序的结构，例如充当应用代码入口点的类。尽管我们可以修改其中的一些规则文件，但某些规则可能由编译时工具（例如 AAPT2）自动生成，或者从应用程序的库依赖中继承。下文描述了 R8 使用的 ProGuard 规则文件的来源、位置以及详细的解读。

- **Android Studio/自定义配置文件：**`<module-dir>/proguard-rules.pro`

  使用 Android Studio 创建新模块时，Android Studio 会在该模块的根目录中创建  `proguard-rules.pro` 文件。默认情况下，该文件内没有任何有效配置。我们在开发时自定义的配置也是写入在该文件中。

- **Android Gradle 插件：**由 Android Gradle 插件在编译时生成

  Android Gradle 插件会生成  `proguard-android-optimize.txt` （其中包含了对大多数 Android 项目都有用的规则），并启用 `@Keep` 注解。默认情况下，使用 Android Studio 创建新模块时，模块级 build 脚本会将此规则文件纳入到发布 build 中，也就是  `getDefaultProguardFile("proguard-android-optimize.txt")`。

- **库依赖项：**AAR： `proguard.txt`、JAR： `META-INF/proguard/<ProGuard-rules-file>`，Android Gradle 插件 3.6 及以上版本还支持针对性的缩减规则。

  AAR 和 JAR 库是可以携带规则文件的，并且将该库作为编译时依赖项纳入到项目中，那么 R8 在编译项目时会自动应用这些规则。例如 [OkHttp](https://square.github.io/okhttp/features/r8_proguard/) 库中就自带了规则文件，我们大多情况下无需针对该库编写规则文件即可让其在 R8 开启下正常进行工作。除了传统的 ProGuard 规则之外，Android Gradle 插件 3.6 或更高版本还支持针对性缩减规则。这些规则针对特定缩减器（R8 或 ProGuard）以及特定缩减器版本。很多库其实都自带了规则文件以保证在 R8 开启下库能够正常进行工作，库的开发者已经为我们进行了适配，保证行为符合预期。另外我们需要注意的是，这些规则文件是累加的，最终打包时会将所有规则文件进行合并，并一起作用与整个打包过程，例如，如果某个库包含停用代码优化的规则，该规则将针对整个项目停用优化，这一点非常重要。合并后的规则文件路径为：`build/outputs/mapping/release/mapping.txt`，下文中会针对该文件及其他文件进行说明。

- **Android 资源打包工具 2 (AAPT2)：** `<module-dir>/build/intermediates/aapt_proguard_file/.../aapt_rules.txt`

  AAPT2 会根据对应用清单中的类、布局及其他应用资源的引用，生成保留规则。例如，AAPT2 会为在应用清单中注册为入口点的每个 activity 添加一个保留规则。

- **其他自定义规则文件：**

  可以通过将其他自定义规则文件添加到模块的 build 脚本的 `proguardFiles` 属性中，添加额外的规则。例如，可以通过在相应的 `productFlavor` 代码块中再添加一个 `proguardFiles` 属性来添加每个 build 变体专用的规则。以下 Gradle 文件会将 `flavor2-rules.pro` 添加到 `flavor2` 产品变种中。现在，`flavor2` 使用全部三个 ProGuard 规则，因为还应用了来自 `release` 代码块的规则。此外，还可以添加 `testProguardFiles` 属性，用于指定仅包含在测试 APK 中的 ProGuard 文件列表：

  ```kotlin
  android {
      ...
      buildTypes {
          getByName("release") {
              isMinifyEnabled = true
              proguardFiles(
                  getDefaultProguardFile("proguard-android-optimize.txt"),
                  "proguard-rules.pro"
              )
              testProguardFiles(
                  // 这里列出的 proguard 文件仅用于生成的测试 APK（包含 androidTest 代码和依赖）。
                	// testProguardFiles 只影响 androidTest，不影响 unitTest。
                  "test-proguard-rules.pro"
              )
          }
      }
      flavorDimensions.add("version")
      productFlavors {
          create("flavor1") {
              ...
          }
          create("flavor2") {
              proguardFile("flavor2-rules.pro")
          }
      }
  }
  ```



> [!CAUTION]
>
> 我们需要注意的一点是，R8 会将多个来源的规则文件进行合并，其他编译时依赖项（如库依赖项）可能会引入我们不了解的 R8 行为变化。

### 缩减代码

如果将 `minifyEnabled` 属性设为 `true`，系统会默认启用 R8 代码缩减功能。

代码缩减（也称为“摇树优化”）是指移除 R8 确定在运行时不需要的代码的过程。此过程可以大大减小应用的大小，例如，当应用包含许多库依赖项，但只使用它们的一小部分功能时。

为了缩减应用的代码，R8 首先会根据合并后的配置文件集确定应用代码的所有入口点。这些入口点包括 Android 平台可用来打开应用的 activity 或服务的所有类。从每个入口点开始，R8 会检查应用的代码来构建一张图表，列出应用在运行时可能会访问的所有方法、成员变量和其他类。系统会将与该图表没有关联的代码视为执行不到的代码，并可能会从应用中移除该代码。

图 1 显示了一个具有运行时库依赖项的应用。R8 通过检查应用的代码，确定可以从 `MainActivity.class` 入口点执行到的 `foo()`、`faz()` 和 `bar()` 方法。不过，应用从未在运行时使用过 `OkayApi.class` 类或其 `baz()` 方法，因此 R8 会在缩减应用时移除该代码。

<img src="https://developer.android.com/static/studio/images/build/r8/tree-shaking.png?hl=zh-cn" alt="R8 检查代码使用" style="zoom:30%;" />

#### 自定义要保留的代码

在大多数情况下，如要让 R8 仅移除不使用的代码，使用默认的 ProGuard 规则文件 (`proguard-android-optimize.txt`) 就已足够。不过，在某些情况下，R8 很难做出正确判断，因而可能会移除应用实际上需要的代码。下面列举了几个示例，说明它在什么情况下可能会错误地移除代码：

- 当应用通过 Java 原生接口 (JNI) 调用方法时
- 当应用在运行时查询代码时（如使用反射）

通过测试应用应该可以发现因错误移除代码而导致的错误，也可以通过生成已移除代码的报告检查移除了哪些代码，在下文中会有详细的解释。

如需修复错误并强制 R8 保留某些代码，请在 ProGuard 规则文件中添加 [`-keep`](https://www.guardsquare.com/en/products/proguard/manual/usage#keepoptions) 代码行。例如：

```
-keep public class MyClass
```

或者，也可以为要保留的代码添加 [`@Keep`](https://developer.android.com/reference/androidx/annotation/Keep?hl=zh-cn) 注解。在类上添加 `@Keep` 可按原样保留整个类。在方法或字段上添加该注释，将使该方法/字段（及其名称）以及类名称保持不变。请注意，只有在使用 [AndroidX 注解库](https://developer.android.com/reference/androidx/annotation/package-summary?hl=zh-cn)且添加 Android Gradle 插件随附的 ProGuard 规则文件时，此注解才可用。

### 缩减资源

资源缩减只有在与代码缩减配合使用时才能发挥作用。在代码缩减器移除所有不使用的代码后，资源缩减器便可确定应用仍要使用的资源，当添加包含资源的代码库时尤其如此。必须移除不使用的库代码，使库资源变为未引用资源，因而可由资源缩减器移除。如需启用资源缩减功能，请将 build 脚本中的 `shrinkResources` 属性（若为代码缩减，则还包括 `minifyEnabled`）设为 `true`。

#### 自定义要保留的资源

如果我们有想要保留或舍弃的特定资源，请在项目中创建一个包含 `<resources>` 标记的 XML 文件，并在 `tools:keep` 属性中指定每个要保留的资源，在 `tools:discard` 属性中指定每个要舍弃的资源。这两个属性都接受以逗号分隔的资源名称列表。可以将星号字符用作通配符。

例如：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@layout/l_used*_c,@layout/l_used_a,@layout/l_used_b*"
    tools:discard="@layout/unused2" />
```

将该文件保存在项目资源中，例如，保存在 `res/raw/my.package.keep.xml` 中。构建系统不会将此文件打包到应用中。

> [!CAUTION]
>
> 请务必为 `keep` 文件使用唯一名称。否则，当不同的库关联在一起时，其 keep 规则会发生冲突，从而导致被忽略的规则或不需要保留的资源可能出现问题。

指定要舍弃的资源可能看似没有必要，因为我们本可将其删除，但在使用 build 变体时，这样做可能很有用。例如，如果我们知道给定资源似乎在代码中使用（因此不会被缩减器移除），但它实际上不会用于给定 build 变体，则可以将所有资源放入公共项目目录，然后为每个 build 变体创建不同的 `my.package.build.variant.keep.xml` 文件。构建工具也可能会错误地将某个资源标识为必需资源，这是因为编译器会内嵌添加资源 ID，而资源分析器可能不知道真正引用的资源与代码中恰好具有相同值的整数值之间的区别。

#### 启用严格引用检查

通常情况下，资源缩减器可以准确地判断某个资源是否被使用。然而，如果代码调用了 `Resources.getIdentifier()`（或者任何库调用了这个方法，例如 [AppCompat](https://developer.android.com/topic/libraries/support-library/features?hl=zh-cn#v7-appcompat) 库会这样做），这意味着代码正在根据动态生成的字符串查找资源名称。在这种情况下，资源缩减器会默认采取防御性行为，将所有符合匹配名称格式的资源标记为可能已使用，无法移除。

例如，以下代码会将所有带 `img_` 前缀的资源标记为已使用。

```kotlin
val name = String.format("img_%1d", angle + 1)
val res = resources.getIdentifier(name, "drawable", packageName)
```

资源缩减器还会查看代码中的所有字符串常量以及各种 `res/raw/` 资源，以查找格式类似于 `file:///android_res/drawable//ic_plus_anim_016.png` 的资源网址。如果它找到与此类似的字符串，或找到其他看似可用来构建与此类似的网址的字符串，则不会将它们移除。

这些是默认情况下启用的安全缩减模式的示例。不过，我们可以停用这种“防患于未然”的处理方式，指定资源缩减器只保留确定要使用的资源。为此，可以将 `keep.xml` 文件中的 `shrinkMode` 设为 `strict`，如下所示：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:shrinkMode="strict" />
```

如果我们确实启用了严格缩减模式，并且代码也通过动态生成的字符串引用资源（如上所示），那么我们必须使用 `tools:keep` 属性手动保留这些资源。

#### 移除未使用的备用资源

Gradle 资源缩减器只会移除未由应用代码引用的资源，这意味着，它不会移除用于不同设备配置的[备用资源](https://developer.android.com/guide/topics/resources/providing-resources?hl=zh-cn#AlternativeResources)。如有必要，可以使用 Android Gradle 插件的 `resConfigs` 属性移除应用不需要的备用资源文件。

例如，如果使用的是包含语言资源的库（如 AppCompat 或 Google Play 服务），那么应用中将包含这些库中消息的所有已翻译语言的字符串，而无论应用的其余部分是否已翻译为相同的语言。如果只想保留应用正式支持的语言，可以使用 `resConfig` 属性指定这些语言。系统会移除未指定语言的所有资源。

以下代码段展示了如何设置只保留英语和法语的语言资源：

```kotlin
android {
    defaultConfig {
        ...
        resourceConfigurations.addAll(listOf("en", "fr"))
    }
}
```

如果使用 Android App Bundle 格式发布应用，那么在默认情况下，安装该应用时只会下载用户设备上配置的语言的应用版本。同样，下载内容中仅包含与设备的屏幕密度相匹配的资源以及与设备的 ABI 相匹配的原生库。

#### 合并重复资源

默认情况下，Gradle 还会合并同名的资源，如可能位于不同资源文件夹中的同名可绘制对象。这一行为不受 `shrinkResources` 属性控制，也无法停用，因为当多个资源与代码查询的名称匹配时，有必要利用这一行为避免错误。只有在两个或更多个文件具有完全相同的资源名称、类型和限定符时，才会进行资源合并。Gradle 会在重复项中选择它认为最合适的文件（根据下述优先顺序），并且只将这一个资源传递给 AAPT，以便在最终工件中分发。

Gradle 会在以下位置查找重复资源：

- 与主源代码集关联的主资源，一般位于 `src/main/res/` 中。
- 变体叠加，来自 build 类型和 build 变种。
- 库项目依赖项。

Gradle 会按以下级联优先顺序合并重复资源：

依赖项 → 主资源 → build 变种 → build 类型

例如，如果某个重复资源同时出现在主资源和 build 变种中，Gradle 会选择 build 变种中的资源。

如果完全相同的资源出现在同一源代码集中，Gradle 无法合并它们，并且会发出资源合并错误。如果在 `build.gradle.kts` 文件的 `sourceSet` 属性中定义了多个源代码集，就可能会发生这种情况。例如，如果 `src/main/res/` 和 `src/main/res2/` 包含完全相同的资源，就可能会发生这种情况。

### 对代码进行混淆处理

混淆处理的目的是通过缩短应用的类、方法和字段的名称来缩减应用的大小。下面是使用 R8 进行混淆处理的一个示例：

```kotlin
androidx.appcompat.app.ActionBarDrawerToggle$DelegateProvider -> a.a.a.b:
androidx.appcompat.app.AlertController -> androidx.appcompat.app.AlertController:
    android.content.Context mContext -> a
    int mListItemLayout -> O
    int mViewSpacingRight -> l
    android.widget.Button mButtonNeutral -> w
    int mMultiChoiceItemLayout -> M
    boolean mShowTitle -> P
    int mViewSpacingLeft -> j
    int mButtonPanelSideLayout -> K
```

虽然混淆处理不会从应用中移除代码，但如果应用的 DEX 文件将许多类、方法和字段编入索引，那么混淆处理将可以显著缩减应用的大小。不过，由于混淆处理会对代码的不同部分进行重命名，因此在执行某些任务（如检查堆栈轨迹）时需要使用额外的工具。

此外，如果代码依赖于应用的方法和类的可预测命名（例如，使用反射时），应该将相应签名视为入口点并为其指定保留规则，如介绍如何自定义要保留的代码的部分中所述。这些保留规则会告知 R8 不仅要在应用的最终 DEX 中保留该代码，而且还要保留其原始命名。也就是我们经常使用的 **Keep** 手段

### 代码优化

为了进一步优化应用，R8 会在更深的层次上检查代码，以移除更多不使用的代码，或者在可能的情况下重写代码，以使其更简洁。下面是此类优化的几个示例：

- 如果代码从未采用过给定 if/else 语句的 else {} 分支，R8 可能会移除 else {} 分支的代码。
- 如果代码只在几个位置调用某个方法，R8 可能会移除该方法并将其内嵌在这几个调用点。
- 如果 R8 确定某个类只有一个唯一子类且该类本身未实例化（例如，一个仅由一个具体实现类使用的抽象基类），它就可以将这两个类组合在一起并从应用中移除一个类。

如需了解详情，请阅读 Jake Wharton 撰写的关于 [R8 优化的博文](https://jakewharton.com/r8-optimization-lambda-groups/)。

R8 不允许停用或启用离散优化，也不允许修改优化的行为。事实上，R8 会忽略试图修改默认优化行为的所有 ProGuard 规则，例如 `-optimizations` 和 `-optimizationpasses`。

> [!CAUTION]
>
> 启用优化将更改应用的堆栈轨迹。例如，进行内嵌会移除堆栈帧。

#### 对运行时性能的影响

如果同时启用缩减、混淆和优化，R8 最多可将代码的运行时性能（包括界面线程上的启动时间和帧时间）提升 30%。停用其中任何一项都会大大限制 R8 使用的优化集。

#### 启用增强型优化（这是个很重要的改动）

R8 包含一组额外的优化功能（称为“完整模式”），这使得它的行为与 ProGuard 不同。从 Android Gradle 插件版本 8.0.0 开始，这些优化功能**默认处于启用状态**。开启这个模式可能会导致各种各样的问题，至少在我最早使用的时候遇到过很多的混淆问题，但是后面很多库都进行了适配。

我们可以通过在项目的 `gradle.properties` 文件中添加以下代码来停用这些额外的优化功能：

```properties
android.enableR8.fullMode=false
```

这些额外的优化功能会使 R8 的行为与 ProGuard 不同，因此如果我们使用的是专为 ProGuard 设计的规则，则可能需要添加额外的 ProGuard 规则，以避免运行时问题。例如，假设代码通过 Java Reflection API 引用一个类。不使用“完整模式”时，R8 会假设我们打算在运行时检查和操纵该类的对象（即使代码实际上并不这样做），因此它会自动保留该类及其静态初始化程序。不过，使用“完整模式”时，R8 不会做出此假设。如果 R8 断言代码在运行时从未使用该类，则会将该类从应用的最终 DEX 中移除。也就是说，如果想保留类及其静态初始化程序，则需要在规则文件中添加保留规则才能实现这一点。

## 三、R8 输出文件解析及用途

在使用 R8 进行代码混淆和优化后，生成的文件位于 `<module-name>/build/outputs/mapping/<build-type>/` 目录下。该目录中包含了一些重要文件，用于调试、错误报告以及了解混淆过程，如下图所示：

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/mapping_files.png?raw=true" alt="首页" style="zoom:25%;" />

### **1. mapping.txt**

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/mapping.png?raw=true" alt="首页" style="zoom:25%;" />  

该文件是 **混淆映射文件**，记录了原始类、方法和字段名称与混淆后的名称之间的映射关系。可以在崩溃报告中恢复混淆前的堆栈跟踪信息，便于调试。可以通过 **retrace** 工具解析错误日志。需要注意的是如果该文件丢失，无法反混淆崩溃日志，会影响问题定位。

### **2. seeds.txt**

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/seeds.png?raw=true" alt="首页" style="zoom:25%;" />  

记录了 **未被混淆的类和成员**，通常是根据 `-keep` 规则指定的内容。该文件可以用来检查 `-keep` 规则是否正确应用。

### **3. usage.txt**

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/usage.png?raw=true" alt="首页" style="zoom:25%;" />  

列出 **被删除的类、字段和方法**，它是代码裁剪的结果。通过该文件我们可以得知哪些未使用的代码被移除，确认关键代码是否被误删，进而使用 `-keep` 规则保护必要代码。

### **4. configuration.txt**

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/configuration.png?raw=true" alt="首页" style="zoom:25%;" />  

最终生成的配置文件，列出 **R8 在运行时实际使用的所有 ProGuard 规则**。该文件包括用户提供的 `proguard-rules.pro` 文件内容、库依赖文件传递过来的规则，以及编译器生成的规则。我们可以借助该文件确认实际应用的混淆规则，确保自定义规则与默认生成规则符合预期，在混淆出现问题时也可以借助该文件排查导致问题的混淆规则。

### **5. resources.txt**

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/resources.png?raw=true" alt="首页" style="zoom:25%;" />  

列出 **资源裁剪器** 移除和保留的资源项。通过该文件确认哪些文件被移除，或者检查是否有哪些必要文件被误删。

## 四、对堆栈轨迹进行还原及相关工具

经过 R8 处理的代码会发生各种更改，这可能会使堆栈轨迹更难以理解，因为堆栈轨迹与源代码不完全一致。如果未保留调试信息，就可能会出现行号更改的情况。这可能是由内嵌和轮廓等优化造成的。影响最大的因素是混淆处理；进行混淆处理时，就连类和方法的名称都会更改。

为了还原原始堆栈轨迹，R8 提供了 retrace 命令行工具，该工具与命令行工具软件包捆绑在一起。

如需支持对应用的堆栈轨迹进行轨迹还原，应通过向模块的 `proguard-rules.pro` 文件添加以下规则来确保 build 保留足够的信息以进行轨迹还原：

```proguard
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
```

`LineNumberTable` 属性会在方法中保留位置信息，以便以堆栈轨迹的形式输出这些位置。`SourceFile` 属性可确保所有可能的运行时都实际输出位置信息。`-renamesourcefileattribute` 指令用于将堆栈轨迹中的源文件名称设置为仅包含 `SourceFile`。在轨迹还原过程中不需要实际的原始源文件名称，因为映射文件中包含原始源文件。

R8 每次运行时都会创建一个 `mapping.txt` 文件，其中包含将堆栈轨迹重新映射为原始堆栈轨迹所需的信息。Android Studio 会将该文件保存在 `<module-name>/build/outputs/mapping/<build-type>/` 目录中。

> [!CAUTION]
>
> 每次构建项目时都会覆盖 Android Studio 生成的 mapping.txt 文件，因此每次发布新版本时都要注意保存一个该文件的副本。通过为每个发布 build 保留一个 mapping.txt 文件的副本，可以在用户提交来自旧版应用的经过混淆处理的堆栈轨迹时进行轨迹还原。

在 Google Play 上发布应用时，可以上传每个应用版本对应的 `mapping.txt` 文件。使用 **Android App Bundle** 格式发布应用时，系统会自动将此文件包含在 app bundle 内容中。然后，Google Play 会根据用户报告的问题对传入的堆栈轨迹进行轨迹还原，以便开发者可以在 Play 管理中心查看这些堆栈轨迹。在其他很多平台中都是支持上传 `mapping.txt` 进行还原堆栈信息的，这里不再进行展开。

#### R8 retrace

R8 retrace 这款工具用于从经过混淆处理的堆栈轨迹获取原始堆栈轨迹。系统会通过在映射文件中对类名和方法名与其原始定义进行匹配来重构堆栈轨迹。该工具需要进行下载，在 SDK 管理器更新工具 - Android SDK Command-line Tools  可以进行下载：

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/command_line_tools.png?raw=true" alt="首页" style="zoom:25%;" />  

该工具位于 `Android SDK目录/cmdline-tools/latest/bin` 中，使用方式：

```shell
retrace  path-to-mapping-file [path-to-stack-trace-file] [options]
```

下图中是我手动抛出一个异常，原始堆栈信息在 `log.txt` 文件中，还原后的堆栈信息在控制台进行输出，我是用的是 **MacOS**：

<img src="https://github.com/Quyunshuo/AndroidR8/blob/main/img/retrace_log.png?raw=true" alt="首页" style="zoom:25%;" />  

记得我之前在使用 **Windows** 时用过一个自带的可视化工具更为方便，但是我现在找不到了... 等我后续找到后继续更新到这里。

## 五、混淆规则及演示

在拆解具体的规则前，我门需要了解 R8 的工作原理。

- 入口点

  为了确定哪些代码需要保留，哪些代码可以丢弃或混淆，必须指定一个或多个入口点。这些入口点在 Android 中通常是一些核心组件类，比如 activity、Service 等。在压缩步骤中，R8 从这些入口点开始，递归地确定哪些类和类成员被使用。所有其他类和类成员将被丢弃。在优化步骤中，R8 进一步优化代码。除了其他优化外，非入口点的类和方法可以被修改为 private、static 或 final，未使用的参数可以被移除，某些方法可能会被内联。在混淆步骤中，R8 会重命名非入口点的类和类成员。在整个过程中，保留入口点可确保它们仍然可以通过原始名称访问。

- **反射**

  反射和自省（introspection）为任何自动化代码处理带来了特殊问题。在 R8 中，你的代码中通过反射动态创建或调用的类或类成员（即通过名称）也必须被指定为入口点。例如，Class.forName() 构造函数可能会在运行时引用任何类。由于类名可能是从配置文件中读取的，因此通常无法计算哪些类必须保留（并保留原始名称）。因此，你必须在配置中指定这些类，使用简单的 `-keep` 选项，或者是使用 `@Keep` 注解。

  然而，ProGuard 已经为你检测并处理了以下情况：

  - Class.forName("SomeClass")
  - SomeClass.class
  - SomeClass.class.getField("someField")
  - SomeClass.class.getDeclaredField("someField")
  - SomeClass.class.getMethod("someMethod", null)
  - SomeClass.class.getMethod("someMethod", new Class[] { A.class,... })
  - SomeClass.class.getDeclaredMethod("someMethod", null)
  - SomeClass.class.getDeclaredMethod("someMethod", new Class[] { A.class,... })
  - AtomicIntegerFieldUpdater.newUpdater(SomeClass.class, "someField")
  - AtomicLongFieldUpdater.newUpdater(SomeClass.class, "someField")
  - AtomicReferenceFieldUpdater.newUpdater(SomeClass.class, SomeType.class, "someField")

  当然，类名和类成员的名称可能不同，但对于 R8 识别这些构造函数，语法必须完全相同。所引用的类和类成员将在压缩阶段保留，字符串参数将在混淆阶段被正确更新。

## 六、R8 常见问题

## 资源

[ProGuard manual](https://www.guardsquare.com/manual/configuration/usage)

[Android Developers 缩减、混淆处理和优化应用](https://developer.android.com/build/shrink-code?hl=zh-cn)

[R8 常见问题解答](https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md)
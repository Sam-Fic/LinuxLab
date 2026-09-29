# Compose / Kotlin 常规保留项
-keep class com.linuxlab.starter.model.** { *; }
-keep class com.linuxlab.starter.data.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-dontobfuscate

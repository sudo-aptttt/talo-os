# ==============================================================================
# TALO OS — R8 / ProGuard Optimization Rules
# Optimized for Google TV Streamer 4K (Android 14 TV)
# ==============================================================================

# Keep Kotlinx Serialization & Ktor models
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Compose and TV UI
-dontwarn androidx.compose.**
-dontwarn androidx.tv.**

# Keep FileProvider
-keep public class androidx.core.content.FileProvider

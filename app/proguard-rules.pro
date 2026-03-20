# Reglas para preservar LibVLC
-keep class org.videolan.libvlc.** { *; }
-keep class org.videolan.medialibrary.** { *; }

# Prevenir que se renombren los métodos nativos (JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

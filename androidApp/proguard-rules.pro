# R8 rules for release builds. Libraries ship their own consumer rules; only gaps are listed here.

# Room: the generated database implementation and the KMP constructor object are created by
# reflection when the database is built.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * implements androidx.room.RoomDatabaseConstructor { *; }

# Ktor: optional JVM-only references (slf4j, JMX, ...) that are not on the Android classpath.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn io.netty.**
-dontwarn reactor.blockhound.**

# kotlinx.serialization: keep generated serializers for @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static **$* *;
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Metro: graphs are generated at compile time with no reflection, so no keep rules are needed.

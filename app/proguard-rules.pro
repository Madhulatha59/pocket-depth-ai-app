# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}

# Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep public class * implements com.google.gson.InstanceCreator

# Pocket Depth AI Data Models
-keep class com.example.pocketdepthai.data.** { *; }

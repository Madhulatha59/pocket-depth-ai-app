# Proguard configuration rules for Periodontal AI Mobile App

# Keep Room database and DAO classes
-keep class * extends androidx.room.RoomDatabase
-keep class com.periodontal.ai.data.model.** { *; }

# Keep SpeechRecognizer components
-keep class android.speech.SpeechRecognizer { *; }

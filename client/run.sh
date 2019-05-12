./gradlew assembleDebug
adb install -r "app/build/outputs/apk/debug/app-debug.apk"
adb shell am start -n com.anjay.crossboard/com.anjay.crossboard.MainActivity
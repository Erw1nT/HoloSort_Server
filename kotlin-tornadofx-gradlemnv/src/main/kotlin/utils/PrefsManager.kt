package utils

import java.util.prefs.Preferences

object HMDLagUserPrefsManager {
    const val OUTPUT_DIRECTORY_KEY = "OUTPUT_DIRECTORY"
    private val prefs : Preferences = Preferences.userNodeForPackage(this::class.java)

    fun savePrefs(key: String, value: String) {
        prefs.put(key, value)
    }

    fun loadPrefs(key: String, defaultValue: String): String {
        return prefs.get(key, defaultValue)
    }
}
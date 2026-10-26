package com.heroes.towerdefense.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Android SharedPreferences implementation of KeyValueStorage.
 */
class SharedPrefsStorage(private val prefs: SharedPreferences) : KeyValueStorage {

    constructor(context: Context, name: String = "heroes_td_preferences") :
            this(context.getSharedPreferences(name, Context.MODE_PRIVATE))

    override fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String?): String? =
        prefs.getString(key, defaultValue)

    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> =
        prefs.getStringSet(key, defaultValue) ?: defaultValue

    override fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}

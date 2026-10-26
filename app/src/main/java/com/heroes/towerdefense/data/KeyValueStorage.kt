package com.heroes.towerdefense.data

/**
 * Key-Value storage abstraction allowing both Android SharedPreferences in production
 * and InMemoryStorage in unit test environments.
 */
interface KeyValueStorage {
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getString(key: String, defaultValue: String?): String?
    fun putString(key: String, value: String?)
    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>
    fun putStringSet(key: String, value: Set<String>)
    fun clear()
}

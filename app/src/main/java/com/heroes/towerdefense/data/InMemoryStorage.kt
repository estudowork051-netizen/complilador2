package com.heroes.towerdefense.data

/**
 * Thread-safe In-Memory implementation of KeyValueStorage for deterministic unit testing.
 */
class InMemoryStorage : KeyValueStorage {
    private val data = mutableMapOf<String, Any>()

    override fun getInt(key: String, defaultValue: Int): Int =
        (data[key] as? Int) ?: defaultValue

    override fun putInt(key: String, value: Int) {
        data[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        (data[key] as? Boolean) ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        data[key] = value
    }

    override fun getString(key: String, defaultValue: String?): String? =
        (data[key] as? String) ?: defaultValue

    override fun putString(key: String, value: String?) {
        if (value != null) {
            data[key] = value
        } else {
            data.remove(key)
        }
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        @Suppress("UNCHECKED_CAST")
        return (data[key] as? Set<String>) ?: defaultValue
    }

    override fun putStringSet(key: String, value: Set<String>) {
        data[key] = value.toSet()
    }

    override fun clear() {
        data.clear()
    }
}

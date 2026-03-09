package com.keisardev.moviesandbeyond.data.repository.impl

import android.content.SharedPreferences

/**
 * A minimal in-memory [SharedPreferences] implementation for unit tests. Only the methods used by
 * [com.keisardev.moviesandbeyond.core.local.session.SessionManager] are fully implemented; other
 * methods throw [UnsupportedOperationException].
 */
class InMemorySharedPreferences : SharedPreferences {
    private val store = mutableMapOf<String, Any?>()
    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): MutableMap<String, *> = store.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? =
        store[key] as? String ?: defValue

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        throw UnsupportedOperationException()

    override fun getInt(key: String?, defValue: Int): Int = store[key] as? Int ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = store[key] as? Long ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = store[key] as? Float ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        store[key] as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = store.containsKey(key)

    override fun edit(): SharedPreferences.Editor = InMemoryEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.add(it) }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.remove(it) }
    }

    private inner class InMemoryEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            key?.let { pending[it] = value }
            return this
        }

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?,
        ): SharedPreferences.Editor {
            key?.let { pending[it] = values }
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            key?.let { pending[it] = value }
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            key?.let { pending[it] = value }
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            key?.let { pending[it] = value }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            key?.let { pending[it] = value }
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            key?.let { pending[it] = null }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clear = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clear) store.clear()
            val changedKeys = pending.keys.toList()
            pending.forEach { (key, value) -> store[key] = value }
            pending.clear()
            changedKeys.forEach { key ->
                listeners.forEach {
                    it.onSharedPreferenceChanged(this@InMemorySharedPreferences, key)
                }
            }
        }
    }
}

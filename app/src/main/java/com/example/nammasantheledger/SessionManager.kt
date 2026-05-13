package com.example.nammasantheledger

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "auth_session"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class SessionManager(private val context: Context) {

    private val loggedInKey = booleanPreferencesKey("logged_in")
    private val emailKey = stringPreferencesKey("logged_in_email")
    private val nameKey = stringPreferencesKey("logged_in_name")

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[loggedInKey] ?: false }

    val loggedInEmail: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[emailKey] ?: "" }

    val loggedInName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[nameKey] ?: "" }

    suspend fun setLoggedIn(email: String, name: String) {
        context.dataStore.edit { prefs: androidx.datastore.preferences.core.MutablePreferences ->
            prefs[loggedInKey] = true
            prefs[emailKey] = email
            prefs[nameKey] = name
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs: androidx.datastore.preferences.core.MutablePreferences ->
            prefs[loggedInKey] = false
            prefs[emailKey] = ""
            prefs[nameKey] = ""
        }
    }
}



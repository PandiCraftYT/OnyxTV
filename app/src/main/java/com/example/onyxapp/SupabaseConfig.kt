package com.example.onyxapp

import android.content.Context
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SupabaseConfig {
    const val SUPABASE_URL = "https://lbhlhkvasgbnnqeaholv.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxiaGxoa3Zhc2dibm5xZWFob2x2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU3NjExOTksImV4cCI6MjA5MTMzNzE5OX0.hOrC4f7w_JC-hIa8D0o6_AZKKDG0DyU_obPvf7nCgg8"

    private var _supabase: SupabaseClient? = null
    val supabase: SupabaseClient
        get() = _supabase ?: throw IllegalStateException("SupabaseClient no ha sido inicializado. Llama a SupabaseConfig.init(context) en tu Application class.")

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    fun init(context: Context) {
        if (_supabase != null) return
        try {
            _supabase = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
                install(Postgrest)
                install(Auth) {
                    sessionManager = SharedPreferencesSessionManager(context, json)
                }
                install(Realtime)
            }
            Log.d("OnyxSupabase", "Supabase inicializado correctamente")
        } catch (e: Exception) {
            Log.e("OnyxSupabase", "Error inicializando Supabase", e)
        }
    }
}

class SharedPreferencesSessionManager(context: Context, private val json: Json) : SessionManager {
    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    override suspend fun saveSession(session: UserSession) {
        try {
            val sessionString = json.encodeToString(session)
            prefs.edit().putString("session", sessionString).apply()
            Log.d("OnyxAuth", "Sesión guardada para: ${session.user?.email}")
        } catch (e: Exception) {
            Log.e("OnyxAuth", "Error al guardar la sesión", e)
        }
    }

    override suspend fun loadSession(): UserSession? {
        val sessionString = prefs.getString("session", null)
        if (sessionString.isNullOrEmpty()) {
            Log.d("OnyxAuth", "No hay sesión persistida para cargar")
            return null
        }
        return try {
            val session = json.decodeFromString<UserSession>(sessionString)
            Log.d("OnyxAuth", "Sesión recuperada para: ${session.user?.email}")
            session
        } catch (e: Exception) {
            Log.e("OnyxAuth", "Error al recuperar la sesión persistida", e)
            null
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove("session").apply()
        Log.d("OnyxAuth", "Sesión persistida eliminada")
    }
}

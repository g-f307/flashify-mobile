package com.example.flashify.model.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        //Singletoon
        private const val AUTH_TOKEN = "auth_token"
        // Para guardar o ID do usuário, para cada um ter seu próprio deck sem conflitar outro
        private const val USER_ID = "user_id"
        const val INVALID_USER_ID = -1
    }

    fun saveAuthData(token: String, userId:Int){
        prefs.edit{
            putString(AUTH_TOKEN,token)
            putInt(USER_ID,userId)
        }
    }

    //Função que obtém o ID do utilizador guardado
    fun getUserId(): Int{
        return prefs.getInt(USER_ID,INVALID_USER_ID)
    }
    fun saveToken(token: String) {
        prefs.edit { putString(AUTH_TOKEN, token) }
    }

    // Dentro da classe TokenManager

    fun getToken(): String? {
        val token = prefs.getString(AUTH_TOKEN, null)
        // Se o token existir, adiciona o prefixo "Bearer ". Caso contrário, retorna nulo.
        return if (token != null) "Bearer $token" else null
    }


    //Função que remove o token e o userID
    fun clearAuthData(){
        prefs.edit{
            remove(AUTH_TOKEN)
            remove(USER_ID)
        }
    }

    // Alias para clearAuthData (compatibilidade)
    fun clearToken() {
        clearAuthData()
    }



}

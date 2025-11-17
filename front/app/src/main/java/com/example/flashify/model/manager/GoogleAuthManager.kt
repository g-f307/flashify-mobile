package com.example.flashify.model.manager

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Gerenciador de Autenticação Google usando Credential Manager API
 * (Método mais moderno e recomendado pelo Google)
 */
class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /**
     * Client ID do Google Cloud Console
     */
    private val webClientId = "320044939291-v61uobjst3vfu9gh3mfh6hjsh8trvkj8.apps.googleusercontent.com"

    /**
     * Inicia o fluxo de login com Google
     * @return GoogleIdToken se bem-sucedido, null caso contrário
     */
    suspend fun signIn(): GoogleSignInResult {
        return withContext(Dispatchers.IO) {
            try {
                // Configura as opções de login do Google
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Permite selecionar qualquer conta
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false) // Não seleciona automaticamente
                    .build()

                // Cria a requisição de credencial
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Solicita a credencial ao usuário
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                // Extrai o token ID do Google
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)

                GoogleSignInResult.Success(
                    idToken = credential.idToken,
                    displayName = credential.displayName ?: "",
                    email = credential.id,
                    profilePictureUrl = credential.profilePictureUri?.toString()
                )

            } catch (e: GetCredentialException) {
                GoogleSignInResult.Error(
                    message = when {
                        e.message?.contains("No credentials available") == true ->
                            "Nenhuma conta Google disponível"
                        e.message?.contains("User cancel") == true ->
                            "Login cancelado"
                        else ->
                            "Erro ao fazer login: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                GoogleSignInResult.Error("Erro inesperado: ${e.message}")
            }
        }
    }
}

/**
 * Resultado do processo de login
 */
sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val displayName: String,
        val email: String,
        val profilePictureUrl: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}
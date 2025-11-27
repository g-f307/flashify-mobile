package com.example.flashify.model.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.example.flashify.model.data.StudyLogRequest
import com.example.flashify.model.data.SubmitQuizRequest
import com.example.flashify.model.database.dao.*
import com.example.flashify.model.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado de conectividade do app
 */
data class ConnectivityState(
    val isOnline: Boolean = false,
    val isWifi: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingSyncCount: Int = 0,
    val lastSyncTimestamp: Long = 0L
)

/**
 * Gerenciador centralizado de sincronização offline/online
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val studyLogDao: StudyLogDao,
    private val quizDao: QuizDao,
    private val quizAttemptDao: QuizAttemptDao
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectivityState = MutableStateFlow(ConnectivityState())
    val connectivityState: StateFlow<ConnectivityState> = _connectivityState.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "📶 Rede disponível")
            updateConnectivityState()
            // Sincronizar automaticamente ao reconectar
            scope.launch {
                syncAll()
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "📵 Rede perdida")
            updateConnectivityState()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateConnectivityState()
        }
    }

    init {
        // Registrar callback de conectividade
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        updateConnectivityState()
    }

    /**
     * Atualiza o estado de conectividade
     */
    private fun updateConnectivityState() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isOnline = capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        _connectivityState.value = _connectivityState.value.copy(
            isOnline = isOnline,
            isWifi = isWifi
        )

        Log.d(TAG, "🌐 Estado de conectividade: online=$isOnline, wifi=$isWifi")

        // Atualizar contagem de itens pendentes
        scope.launch {
            updatePendingSyncCount()
        }
    }

    /**
     * Atualiza a contagem de itens pendentes de sincronização
     */
    private suspend fun updatePendingSyncCount() {
        val userId = tokenManager.getUserId()
        if (userId == TokenManager.INVALID_USER_ID) return

        val unsyncedLogs = studyLogDao.getUnsyncedLogs(userId).size
        val unsyncedAttempts = quizAttemptDao.getUnsyncedAttempts(userId).size

        _connectivityState.value = _connectivityState.value.copy(
            pendingSyncCount = unsyncedLogs + unsyncedAttempts
        )

        Log.d(TAG, "📊 Pendentes: $unsyncedLogs logs, $unsyncedAttempts tentativas")
    }

    /**
     * Verifica se está online
     */
    fun isOnline(): Boolean = _connectivityState.value.isOnline

    /**
     * Sincroniza todos os dados pendentes
     */
    suspend fun syncAll(): Boolean {
        if (!isOnline()) {
            Log.w(TAG, "⚠️ Tentativa de sync offline - abortado")
            return false
        }

        if (_connectivityState.value.isSyncing) {
            Log.d(TAG, "🔄 Sync já em progresso")
            return false
        }

        Log.d(TAG, "🔄 Iniciando sincronização completa")
        _connectivityState.value = _connectivityState.value.copy(isSyncing = true)

        try {
            val userId = tokenManager.getUserId()
            if (userId == TokenManager.INVALID_USER_ID) {
                Log.e(TAG, "❌ Usuário inválido")
                return false
            }

            // 1. Sincronizar logs de estudo
            Log.d(TAG, "📝 Iniciando sync de logs de estudo...")
            syncStudyLogs(userId)

            // 2. Sincronizar tentativas de quiz
            Log.d(TAG, "🎯 Iniciando sync de tentativas de quiz...")
            syncQuizAttempts(userId)

            // 3. Atualizar timestamp
            _connectivityState.value = _connectivityState.value.copy(
                lastSyncTimestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "✅ Sincronização completa bem-sucedida!")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na sincronização: ${e.message}", e)
            return false

        } finally {
            _connectivityState.value = _connectivityState.value.copy(isSyncing = false)
            updatePendingSyncCount()
        }
    }

    private suspend fun syncStudyLogs(userId: Int) {
        val token = tokenManager.getToken() ?: return
        val unsyncedLogs = studyLogDao.getUnsyncedLogs(userId)

        Log.d(TAG, "📝 Encontrados ${unsyncedLogs.size} logs não sincronizados")

        unsyncedLogs.forEach { log ->
            try {
                Log.d(TAG, "📤 Enviando log ${log.localId} para servidor...")
                apiService.logStudy(
                    token,
                    log.flashcardId,
                    StudyLogRequest(accuracy = log.accuracy)
                )

                studyLogDao.markLogAsSynced(log.localId)
                Log.d(TAG, "✅ Log ${log.localId} marcado como sincronizado")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao sincronizar log ${log.localId}: ${e.message}")
            }
        }
    }

    private suspend fun syncQuizAttempts(userId: Int) {
        val token = tokenManager.getToken() ?: return
        val unsyncedAttempts = quizAttemptDao.getUnsyncedAttempts(userId)

        Log.d(TAG, "🎯 Encontradas ${unsyncedAttempts.size} tentativas não sincronizadas")

        unsyncedAttempts.forEach { attempt ->
            try {
                Log.d(TAG, "📤 Enviando tentativa ${attempt.localId} para servidor...")
                val request = SubmitQuizRequest(
                    score = attempt.score,
                    correctAnswers = attempt.correctAnswers,
                    totalQuestions = attempt.totalQuestions
                )

                apiService.submitQuizAttempt(token, attempt.quizId, request)

                quizAttemptDao.markAttemptAsSynced(attempt.localId)
                Log.d(TAG, "✅ Tentativa ${attempt.localId} marcada como sincronizada")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao sincronizar tentativa ${attempt.localId}: ${e.message}")
            }
        }
    }

    /**
     * Força uma sincronização manual
     */
    fun forceSyncNow() {
        scope.launch {
            syncAll()
        }
    }

    /**
     * Limpa o estado de sincronização
     */
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
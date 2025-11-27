//package com.example.flashify.model.worker
//
//import android.content.Context
//import android.util.Log
//import androidx.hilt.work.HiltWorker
//import androidx.work.*
//import com.example.flashify.model.manager.SyncManager
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.util.concurrent.TimeUnit
//
///**
// * Worker para sincronização automática em background
// */
//@HiltWorker
//class SyncWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val syncManager: SyncManager
//) : CoroutineWorker(appContext, workerParams) {
//
//    companion object {
//        const val TAG = "SyncWorker"
//        private const val UNIQUE_WORK_NAME = "flashify_sync_work"
//
//        /**
//         * Agenda sincronização periódica
//         */
//        fun schedulePeriodicSync(context: Context) {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED) // Só executar se houver rede
//                .build()
//
//            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
//                15, TimeUnit.MINUTES // Sincronizar a cada 15 minutos
//            )
//                .setConstraints(constraints)
//                .setBackoffCriteria(
//                    BackoffPolicy.EXPONENTIAL,
//                    WorkRequest.MIN_BACKOFF_MILLIS,
//                    TimeUnit.MILLISECONDS
//                )
//                .build()
//
//            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                UNIQUE_WORK_NAME,
//                ExistingPeriodicWorkPolicy.KEEP, // Manter o trabalho existente
//                syncRequest
//            )
//
//            Log.d(TAG, "✅ Sincronização periódica agendada")
//        }
//
//        /**
//         * Força uma sincronização imediata
//         */
//        fun syncNow(context: Context) {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//
//            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
//                .setConstraints(constraints)
//                .build()
//
//            WorkManager.getInstance(context).enqueue(syncRequest)
//            Log.d(TAG, "🔄 Sincronização imediata solicitada")
//        }
//    }
//
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        Log.d(TAG, "🔄 Iniciando sincronização em background")
//
//        return@withContext try {
//            val success = syncManager.syncAll()
//
//            if (success) {
//                Log.d(TAG, "✅ Sincronização em background bem-sucedida")
//                Result.success()
//            } else {
//                Log.w(TAG, "⚠️ Sincronização falhou - tentando novamente")
//                Result.retry()
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Erro na sincronização: ${e.message}", e)
//            Result.retry()
//        }
//    }
//}
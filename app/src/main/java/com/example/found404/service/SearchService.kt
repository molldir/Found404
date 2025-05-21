import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.found404.R
import java.util.*
import android.os.Build

class SearchService : Service() {
    private val CHANNEL_ID = "search_service_channel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_DEFAULT
        val notificationText = when (type) {
            TYPE_LOST -> "Идет поиск вещи..."
            TYPE_FOUND -> "Идет поиск владельца..."
            else -> "Поиск..."
        }

        // Запуск Foreground Service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Found404")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_search_notification)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        Log.d("SearchService", "Сервис запущен с типом: $type")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SearchService", "Сервис остановлен")
    }

    // Создаем канал уведомлений для Android 8+
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Search Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Сервис для поиска объявлений"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val EXTRA_TYPE = "announcement_type"
        const val TYPE_LOST = "lost"
        const val TYPE_FOUND = "found"
        const val TYPE_DEFAULT = "default"
        const val CHANNEL_ID = "search_service_channel"
    }
}
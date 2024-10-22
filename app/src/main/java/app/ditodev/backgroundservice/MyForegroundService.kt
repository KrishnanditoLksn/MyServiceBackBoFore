package app.ditodev.backgroundservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_01"
        private const val CHANNEL_NAME = "dito channel"
        internal val TAG = MyForegroundService::class.java.simpleName
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notif)
        }
        Log.d(TAG, "Service dijalankan...")
        serviceScope.launch {
            for (i in 1..50) {
                delay(1000)
                Log.d(TAG, "Do Something $i")
            }
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
            Log.d(MyBackgroundService.TAG, "Service dihentikan")
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Satt  ini foreground service sedang berjalan")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = CHANNEL_NAME
        notificationBuilder.setChannelId(CHANNEL_ID)
        mNotificationManager.createNotificationChannel(channel)

        return notificationBuilder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "onDestroy: Service dihentikan")
    }
}
package app.ditodev.backgroundservice

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyBoundService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    val numberLiveData: MutableLiveData<Int> = MutableLiveData()

    companion object {
        private val TAG = MyBoundService::class.java.simpleName
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind:")
        serviceScope.launch {
            for (i in 1..50) {
                delay(1000)
                Log.d(TAG, "Something $i")
                numberLiveData.postValue(i)
            }
            Log.d(TAG, "Service stopped")
        }
        return binder
    }

    private var binder = MyBinder()

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "unbind : ")
        serviceJob.cancel()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onrebind")
    }

    inner class MyBinder : Binder() {
        val getService: MyBoundService = this@MyBoundService
    }
}
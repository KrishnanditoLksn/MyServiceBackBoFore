package app.ditodev.backgroundservice

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.ditodev.backgroundservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var boundStatus = false
    private lateinit var boundService: MyBoundService
    private var connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myBinder = service as MyBoundService.MyBinder
            boundService = myBinder.getService
            boundStatus = true
            getNumberFromService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            boundStatus = false
        }

    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (!isGranted!!) {
            Toast.makeText(
                this, "Unable to display Foreground service notification due to permission decline",
                Toast.LENGTH_LONG
            )
        }
    }

    private fun getNumberFromService() {
        boundService.numberLiveData.observe(this) {
            binding.tvBoundServiceNumber.text = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        //foreground
        val foregroundServiceIntent = Intent(this, MyForegroundService::class.java)
        binding.btnStartForegroundService.setOnClickListener {
            startForegroundService(foregroundServiceIntent)
        }

        binding.btnStopForegroundService.setOnClickListener {
            stopService(foregroundServiceIntent)
        }

        //background
        val serviceIntent = Intent(this, MyBackgroundService::class.java)
        binding.btnStartBackgroundService.setOnClickListener {
            startService(serviceIntent)
        }

        binding.btnStopBackgroundService.setOnClickListener {
            stopService(serviceIntent)
        }

        //bound
        val boundServiceIntent = Intent(this, MyBoundService::class.java)
        binding.btnStartBoundService.setOnClickListener {
            bindService(boundServiceIntent, connection, BIND_AUTO_CREATE)
        }

        binding.btnStopBoundService.setOnClickListener {
            unbindService(connection)
        }
    }

    override fun onStop() {
        super.onStop()
        if (boundStatus) {
            unbindService(connection)
            boundStatus = false
        }
    }
}
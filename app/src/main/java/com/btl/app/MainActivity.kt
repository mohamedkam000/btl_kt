package com.btl.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.btl.app.ui.theme.AppMaterialTheme
import com.google.android.material.color.DynamicColors
import java.io.IOException
import java.io.OutputStream
import java.util.UUID



class MainActivity : ComponentActivity() {

    private val myUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            var status by remember { mutableStateOf("Disconnected") }
            var name by remember { mutableStateOf("عبدالله عثمان إدريس عبدالله") }

            AppMaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer (modifier
Modifier.height(100. dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 30.dp)
                        )

                        Text(
                            text = status,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Button(
                            onClick = {
                                showPairedDevices { deviceName ->
                                    status = deviceName
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        ) {
                            Text("Connect")
                        }

                        Button(
                            onClick = {
                                sendUnlockCommand(
                                    onSuccess = {
                                        Toast.makeText(this@MainActivity, "Unlock command has been sent", Toast.LENGTH_SHORT).show()
                                    },
                                    onFail = {
                                        Toast.makeText(this@MainActivity, "Not connected", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock")
                        }
                    }
                }
            }
        }
    }

    private fun showPairedDevices(onConnected: (String) -> Unit) {
        val adapter = bluetoothAdapter ?: run {
            Toast.makeText(this, "Bluetooth unsupported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }

        val pairedDevices = adapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            return
        }

        val list = pairedDevices.toList()
        val first = list.first()
        selectedDevice = first
        connectBluetooth()
        onConnected("Connected to ${first.name}")
    }

    private fun connectBluetooth() {
        val device = selectedDevice ?: return

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
        } catch (e: IOException) {
            try { bluetoothSocket?.close() } catch (_: IOException) {}
        }
    }

    private fun sendUnlockCommand(onSuccess: () -> Unit, onFail: () -> Unit) {
        val out = outputStream ?: run {
            onFail()
            return
        }

        try {
            out.write("Unlock".toByteArray())
            onSuccess()
        } catch (e: IOException) {
            onFail()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { bluetoothSocket?.close() } catch (_: IOException) {}
    }
}
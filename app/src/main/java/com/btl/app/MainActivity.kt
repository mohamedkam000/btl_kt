package com.btl.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val myUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null

    private lateinit var statusText: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnUnlock: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) showPairedDevices()
            else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        btnConnect = findViewById(R.id.btnConnect)
        btnUnlock = findViewById(R.id.btnUnlock)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        btnConnect.setOnClickListener { showPairedDevices() }
        btnUnlock.setOnClickListener { sendUnlockCommand() }
    }

    private fun showPairedDevices() {
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

        val devices = pairedDevices.toList()
        val names = devices.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Choose a device to connect to")
            .setItems(names) { _, which ->
                selectedDevice = devices[which]
                connectBluetooth()
            }
            .show()
    }

    private fun connectBluetooth() {
        val device = selectedDevice ?: run {
            statusText.text = "No device was chosen"
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            statusText.text = "Missing Bluetooth permission"
            return
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            statusText.text = "Successfully connected to ${device.name}"
        } catch (e: IOException) {
            statusText.text = "Connection failed: ${e.message}"
            try {
                bluetoothSocket?.close()
            } catch (_: IOException) {
            }
        }
    }

    private fun sendUnlockCommand() {
        val out = outputStream ?: run {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            out.write("Unlock".toByteArray())
            Toast.makeText(this, "Unlock command has been sent", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to communicate", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (_: IOException) {
        }
    }
}
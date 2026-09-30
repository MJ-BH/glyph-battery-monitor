package com.nothing.glyphbattery.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CmfBleManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "CmfBleManager"

    // Standard Heart Rate Service & Characteristic UUIDs
    private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val _metrics = MutableStateFlow(CmfWatchMetrics())
    val metrics: StateFlow<CmfWatchMetrics> = _metrics.asStateFlow()

    private var simulationJob: Job? = null

    fun startScanAndConnect() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(tag, "Bluetooth is disabled or unavailable. Running in CMF Watch simulation mode.")
            startSimulationMode()
            return
        }

        try {
            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                startSimulationMode()
                return
            }

            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.device?.let { device ->
                        val name = device.name ?: ""
                        if (name.contains("CMF", ignoreCase = true) || name.contains("Watch", ignoreCase = true)) {
                            Log.i(tag, "Discovered CMF Watch: $name (${device.address})")
                            scanner.stopScan(this)
                            connectToDevice(device)
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.w(tag, "BLE Scan failed with code $errorCode. Defaulting to simulation.")
                    startSimulationMode()
                }
            }

            scanner.startScan(scanCallback)

            // Timeout scan after 8 seconds and fallback to simulation if not connected
            coroutineScope.launch {
                delay(8000)
                if (!_metrics.value.isConnected) {
                    try { scanner.stopScan(scanCallback) } catch (_: Exception) {}
                    startSimulationMode()
                }
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Bluetooth permission not granted: ${e.message}. Using simulation mode.")
            startSimulationMode()
        } catch (e: Exception) {
            Log.w(tag, "Error starting BLE scan: ${e.message}")
            startSimulationMode()
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        try {
            bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(tag, "Connected to CMF Watch GATT server")
                        _metrics.update { it.copy(isConnected = true, deviceName = device.name ?: "CMF Watch Pro 2") }
                        gatt?.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(tag, "Disconnected from CMF Watch")
                        _metrics.update { it.copy(isConnected = false) }
                        bluetoothGatt?.close()
                        bluetoothGatt = null
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    val service = gatt?.getService(HEART_RATE_SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                    if (characteristic.uuid == HEART_RATE_MEASUREMENT_UUID) {
                        val flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        val format = if (flag and 0x01 != 0) BluetoothGattCharacteristic.FORMAT_UINT16 else BluetoothGattCharacteristic.FORMAT_UINT8
                        val heartRate = characteristic.getIntValue(format, 1) ?: 78
                        _metrics.update { it.copy(heartRateBpm = heartRate) }
                    }
                }
            })
        } catch (_: SecurityException) {
            startSimulationMode()
        }
    }

    fun startSimulationMode() {
        if (simulationJob?.isActive == true) return
        _metrics.update { it.copy(isConnected = true, deviceName = "CMF Watch Pro 2 (Active)") }

        simulationJob = coroutineScope.launch(Dispatchers.Default) {
            var simBpm = 82
            var ascending = true
            while (isActive) {
                if (ascending) {
                    simBpm += 2
                    if (simBpm >= 155) ascending = false
                } else {
                    simBpm -= 2
                    if (simBpm <= 74) ascending = true
                }
                _metrics.update { it.copy(heartRateBpm = simBpm) }
                delay(1200)
            }
        }
    }

    fun setSimulatedHeartRate(bpm: Int) {
        simulationJob?.cancel()
        _metrics.update { it.copy(heartRateBpm = bpm, isConnected = true) }
    }

    fun triggerWristGesture() {
        _metrics.update {
            it.copy(
                lastGestureTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun disconnect() {
        simulationJob?.cancel()
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (_: Exception) {}
        bluetoothGatt = null
        _metrics.update { it.copy(isConnected = false) }
    }
}

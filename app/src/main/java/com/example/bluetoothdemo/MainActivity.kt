package com.example.bluetoothdemo

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bluetooth.manager.BleManager
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattWriteCallback
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetoothdemo.databinding.ActivityMainBinding

/**
Create by yangyan
Create time:2023/9/3 00:02
Describe:
 */
class MainActivity:AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT),0)

        BleManager.instance.gatt.connectByAddress("BC:6B:FF:14:9C:64",object :IConnectCallback{
            override fun onPairSuccess() {
                super.onPairSuccess()
                Log.d("BleManager","onPairSuccess:")
            }

            override fun onPairing() {
                super.onPairing()
                Log.d("BleManager","onPairing:")
            }

            override fun onPairFailure(exception: BleException) {
                super.onPairFailure(exception)
                Log.d("BleManager","onPairFailure:"+exception.description)

            }
            override fun onConnectFailure(exception: BleException) {
                Log.d("BleManager","onConnectFailure:"+exception.description)
            }

            override fun onConnectSuccess(bleDevice: BluetoothLeDevice, bleGatt: BluetoothGatt?) {
                Log.d("BleManager","onConnectSuccess:"+bleDevice.getDeviceName())
                val currentConnectDevice=BleManager.instance.gatt.getConnectedDeviceByAddress(bleDevice.getAddress())
                Log.d("BleManager","currentConnectDevice:"+currentConnectDevice.toString())
//                BleManager.instance.gatt.startHeartbeat(bleDevice,"ping",2000)
                BleManager.instance.gatt.addConnectStateListener(bleDevice,object :IBleConnectStateListener{
                    override fun onConnectState(isConnect: Boolean) {
                        Log.d("BleManager","onConnectState:"+isConnect)
                    }
                })

                BleManager.instance.gatt.writeMsg(bleDevice,"ping".toByteArray(),object :IBleGattWriteCallback{
                    override fun onWriteSuccess(justWrite: ByteArray?) {
                        Log.d("BleManager","writeMsg:"+ justWrite?.let { String(it) })
                    }

                    override fun onWriteFailure(exception: BleException) {
                        Log.d("BleManager","writeMsg:"+exception.description)
                    }
                })

                BleManager.instance.gatt.writeMsg(bleDevice,"123456789".toByteArray(),object :IBleGattWriteCallback{
                    override fun onWriteSuccess(justWrite: ByteArray?) {
                        Log.d("BleManager","writeMsg:"+ justWrite?.let { String(it) })
                    }

                    override fun onWriteFailure(exception: BleException) {
                        Log.d("BleManager","writeMsg:"+exception.description)
                    }
                })



            }

        },20000,20000)


//        BleScanCallbackManager.instance.startScan(ScanBluetoothCallback(object : IScanCallback {
//            override fun onDeviceFound(bluetoothLeDevice: BluetoothLeDevice) {
//               Log.d("BleScanCallbackManager",bluetoothLeDevice.getDeviceName().toString()+"  "+bluetoothLeDevice.getAddress())
//            }
//
//            override fun onScanFinish(devices: List<BluetoothLeDevice>) {
//                Log.d("BleScanCallbackManager","onScanFinish:"+devices.size)
//            }
//
//            override fun onScanTimeout() {
//                Log.d("BleScanCallbackManager","超时:")
//            }
//
//            override fun onFailure(bleException: BleException) {
//                Log.d("BleScanCallbackManager","onFailure:"+bleException.toString())
//            }
//
//        },object :IScanCallbackFilter{
//            override fun onFilter(bluetoothLeDevice: BluetoothLeDevice): BluetoothLeDevice? {
//                if(bluetoothLeDevice.getAddress()=="BC:6B:FF:14:9B:86"){
//                    return bluetoothLeDevice
//                }
//                return null
//            }
//        }))
    }
}
package com.example.bluetoothdemo

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bluetooth.manager.BleManager
import com.example.bluetooth.manager.common.BleConfig.Companion.instance
import com.example.bluetooth.manager.common.MergeWholeMsgRule.Companion.rule
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.exception.BleException
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

        val rule = rule.setMergeHeadFlag("a55a", 2)
            .setMergeMessageByteSize(4)
        instance
            .setUUidToSPP("00001101-0000-1000-8000-00805F9B34FB")
            .setMergeWholeMsgRule(rule)
            .init(this)
        connect("BC:6B:FF:14:9B:86")
    }

    private fun connect(ip:String){
        Log.d("BleManager","开始连接:")
        BleManager.Rfcomm.instance.connectByAddress(ip,object :IConnectCallback{
            override fun onPairSuccess() {
                super.onPairSuccess()
                connect(ip)
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
//                Log.d("BleManager","onConnectSuccess:"+bleDevice.getDeviceName())
//                val currentConnectDevice=BleManager.Gatt.instance.getConnectedDeviceByAddress(bleDevice.getAddress())
//                Log.d("BleManager","currentConnectDevice:"+currentConnectDevice.toString())
////                BleManager.instance.gatt.startHeartbeat(bleDevice,"ping",2000)
//                BleManager.Gatt.instance.addConnectStateListener(bleDevice,object :IBleConnectStateListener{
//                    override fun onConnectState(isConnect: Boolean) {
//                        Log.d("BleManager","onConnectState:"+isConnect)
//                    }
//                })
//
//                BleManager.Gatt.instance.writeMsg(bleDevice,"ping".toByteArray(),object :IBleGattWriteCallback{
//                    override fun onWriteSuccess(justWrite: ByteArray?) {
//                        Log.d("BleManager","writeMsg:"+ justWrite?.let { String(it) })
//                    }
//
//                    override fun onWriteFailure(exception: BleException) {
//                        Log.d("BleManager","writeMsg:"+exception.description)
//                    }
//                })
//
//                BleManager.Gatt.instance.writeMsg(bleDevice,"123456789".toByteArray(),object :IBleGattWriteCallback{
//                    override fun onWriteSuccess(justWrite: ByteArray?) {
//                        Log.d("BleManager","writeMsg:"+ justWrite?.let { String(it) })
//                    }
//
//                    override fun onWriteFailure(exception: BleException) {
//                        Log.d("BleManager","writeMsg:"+exception.description)
//                    }
//                })
                BleManager.Rfcomm.instance.addConnectStateListener(bleDevice,object :IBleConnectStateListener{
                    override fun onConnectState(isConnect: Boolean) {
                        Log.d("BleManager","收到的内容为,连接："+isConnect)
                    }

                })

                BleManager.Rfcomm.instance.startHeartbeat(bleDevice,"ping",2000)
                BleManager.Rfcomm.instance.addMessageListener(bleDevice,object :IBleReadCallback{
                    override fun onReadSuccess(data: ByteArray) {
//                        BleManager.Rfcomm.instance.writeMsg(bleDevice,"{\"args\": {}, \"op\": \"call_service\", \"service\": \"/device_info/get_current_network\", \"timeout\": 15}")
                    }

                    override fun onReadFailure(exception: BleException) {
                        Log.d("BleManager","收到的内容为：1111")
                    }

                })

            }

        },2000,20000)

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
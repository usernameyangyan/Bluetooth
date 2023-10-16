package com.example.bluetooth.manager.connect.rfcomm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.bluetooth.manager.BleManager
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IGetConnectedDeviceListener
import com.example.bluetooth.manager.connect.impl.IAutoConnectImpl
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetooth.manager.scan.BleScanCallbackManager
import com.example.bluetooth.manager.scan.impl.IScanCallback
import com.example.bluetooth.manager.scan.impl.IScanCallbackFilter
import com.example.bluetooth.manager.scan.impl.ScanBluetoothCallback
import com.example.bluetooth.manager.utils.BleUtils

/**
Create by yangyan
Create time:2023/8/31 10:16
Describe:rfcomm 管理类
 */
class RfcommConnectManager : ICommonRfcommImpl, IAutoConnectImpl, IGetConnectedDeviceListener {
    companion object {
        val instance: RfcommConnectManager by lazy { RfcommConnectManager() }
    }

    private var multipleBluetoothOpera: MultipleRfcommBluetoothOpera =
        MultipleRfcommBluetoothOpera()

    /**
     * 获取连接的蓝牙设备
     */
    fun getMultiRfcommOpera(): MultipleRfcommBluetoothOpera {
        return multipleBluetoothOpera
    }

    fun disconnect(bleDevice: BluetoothLeDevice) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)?.disconnect()
    }

    fun disconnectAllDevice() {
        multipleBluetoothOpera.disconnectAllDevice()
    }

    override fun connect(
        bluetoothLeDevice: BluetoothLeDevice,
        connectCallback: IConnectCallback,
        connectTimeout: Long?
    ) {
        if (connectBeforeCheck(connectCallback)) {
            val rfcommConnectMirror = RfcommConnectMirror(bluetoothLeDevice)
            rfcommConnectMirror.connect(bluetoothLeDevice, connectCallback, connectTimeout)
        }
    }

    override fun connectByAddress(
        address: String,
        callback: IConnectCallback,
        connectTimeout: Long?,
        scanTimeout: Long?
    ) {
        BleManager.BluetoothScan.instance.startScan(ScanBluetoothCallback(object :IScanCallback{
            override fun onDeviceFound(bluetoothLeDevice: BluetoothLeDevice) {
                connect(bluetoothLeDevice,callback,connectTimeout)
            }

            override fun onScanFinish(devices: List<BluetoothLeDevice>) {

            }

            override fun onScanTimeout() {
                callback.onConnectFailure(BleException(BleExceptionCode.SCAN_ERROR,"Bluetooth scan timeout"))
            }

            override fun onFailure(bleException: BleException) {
                callback.onConnectFailure(bleException)
            }

        },object :IScanCallbackFilter{
            override fun onFilter(bluetoothLeDevice: BluetoothLeDevice): BluetoothLeDevice? {
                if(bluetoothLeDevice.getAddress()==address){
                    BleScanCallbackManager.instance.stopScan()
                    return bluetoothLeDevice
                }
                return null
            }

        }),scanTimeout)
    }

    private fun connectBeforeCheck(connectCallback: IConnectCallback): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                BleConfig.instance.getContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            connectCallback.onConnectFailure(
                BleException(
                    BleExceptionCode.OTHER_FAIL,
                    "Bluetooth connect need permissions:${Manifest.permission.BLUETOOTH_CONNECT}"
                )
            )
            return false
        }
        if (!BleUtils.isBleEnable(BleConfig.instance.getContext())) {
            connectCallback.onConnectFailure(
                BleException(
                    BleExceptionCode.OTHER_FAIL,
                    "Bluetooth not enable!"
                )
            )
            return false
        }
        return true
    }

    override fun writeMsg(bleDevice: BluetoothLeDevice, msg: String): Boolean {
        if (multipleBluetoothOpera.getRfcommConnectMirror(bleDevice) != null) {
            return multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)!!
                .writeMsg(bleDevice, msg)
        }
        return false
    }

    override fun writeMsg(bleDevice: BluetoothLeDevice, bytes: ByteArray): Boolean {
        if (multipleBluetoothOpera.getRfcommConnectMirror(bleDevice) != null) {
            return multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)!!
                .writeMsg(bleDevice, bytes)
        }
        return false
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice,
        heartContent: String,
        interval: Long
    ) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.startHeartbeat(bleDevice, heartContent, interval)
    }

    override fun startHeartbeat(bleDevice: BluetoothLeDevice, bytes: ByteArray, interval: Long) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.startHeartbeat(bleDevice, bytes, interval)
    }

    override fun addMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback
    ) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.addMessageListener(bleDevice, bleReadCallback)
    }

    override fun removeMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback
    ) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.removeMessageListener(bleDevice, bleReadCallback)
    }

    override fun addConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.addConnectStateListener(bleDevice, bleConnectStateListener)
    }

    override fun removeConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        multipleBluetoothOpera.getRfcommConnectMirror(bleDevice)
            ?.removeConnectStateListener(bleDevice, bleConnectStateListener)
    }

    override fun getConnectedDeviceByAddress(address: String): BluetoothLeDevice? {
        return multipleBluetoothOpera.getConnectedDeviceByAddress(address)
    }
}
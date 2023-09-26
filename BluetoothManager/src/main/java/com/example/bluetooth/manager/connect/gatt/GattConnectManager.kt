package com.example.bluetooth.manager.connect.gatt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.bluetooth.manager.BleManager
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.common.BleConstant
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattMtuChangedCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattNotifyCallback
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattRssiCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattWriteCallback
import com.example.bluetooth.manager.connect.gatt.impl.ICommonGattImpl
import com.example.bluetooth.manager.connect.impl.IAutoConnectImpl
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.connect.impl.IGetConnectedDeviceListener
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetooth.manager.scan.BleScanCallbackManager
import com.example.bluetooth.manager.scan.impl.IScanCallback
import com.example.bluetooth.manager.scan.impl.IScanCallbackFilter
import com.example.bluetooth.manager.scan.impl.ScanBluetoothCallback
import com.example.bluetooth.manager.utils.BleUtils

/**
Create by yangyan
Create time:2023/8/31 16:00
Describe:gatt 操作管理类
 */
class GattConnectManager : ICommonGattImpl, IAutoConnectImpl, IGetConnectedDeviceListener {
    companion object {
        val instance: GattConnectManager by lazy { GattConnectManager() }
    }

    private var multipleGattBluetoothOpera: MultipleGattBluetoothOpera =
        MultipleGattBluetoothOpera()

    /**
     * 获取连接的蓝牙设备
     */
    fun getMultiGattOpera(): MultipleGattBluetoothOpera {
        return multipleGattBluetoothOpera
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param bleRssiCallback
     */
    override fun addRssiListener(
        bleDevice: BluetoothLeDevice,
        bleRssiCallback: IBleGattRssiCallback
    ) {
        val gattConnectMirror = multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
        if (gattConnectMirror == null) {
            bleRssiCallback.onRssiFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "This device is not connected!"
                )
            )
        } else {
            gattConnectMirror.addRssiListener(bleDevice, bleRssiCallback)
        }
    }

    override fun removeRssiListener(
        bleDevice: BluetoothLeDevice,
        bleRssiCallback: IBleGattRssiCallback
    ) {
        val gattConnectMirror = multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
        if (gattConnectMirror == null) {
            bleRssiCallback.onRssiFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "This device is not connected!"
                )
            )
        } else {
            gattConnectMirror.removeRssiListener(bleDevice, bleRssiCallback)
        }
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuidService
     * @param uuidWrite
     * @param data
     * @param bleWriteCallback
     */
    override fun writeMsg(
        bleDevice: BluetoothLeDevice,
        data: ByteArray?,
        bleWriteCallback: IBleGattWriteCallback?,
        uuidService: String?,
        uuidWrite: String?
    ) {
        val gattConnectMirror = multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
        if (gattConnectMirror == null) {
            bleWriteCallback?.onWriteFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "This device is not connected!"
                )
            )
        } else {
            gattConnectMirror.writeMsg(bleDevice, data, bleWriteCallback, uuidService, uuidWrite)
        }
    }

    override fun addReadMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback,
        uuidService: String?,
        uuidRead: String?
    ) {
        val gattConnectMirror = multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
        if (gattConnectMirror == null) {
            bleReadCallback.onReadFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "This device is not connected!"
                )
            )
        } else {
            gattConnectMirror.addReadMessageListener(
                bleDevice,
                bleReadCallback,
                uuidService,
                uuidRead
            )
        }
    }

    /**
     * 消息通知
     */
    override fun addNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?,
        uuidNotify: String?,
        userCharacteristicDescriptor: Boolean?
    ) {
        val gattConnectMirror = multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
        if (gattConnectMirror == null) {
            bleNotifyCallback.onNotifyFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "This device is not connected!"
                )
            )
        } else {
            gattConnectMirror.addNotifyMessageListener(
                bleDevice,
                bleNotifyCallback,
                uuidService,
                uuidNotify,
                userCharacteristicDescriptor
            )
        }
    }

    override fun removeNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?,
        uuidNotify: String?,
        userCharacteristicDescriptor: Boolean?
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.removeNotifyMessageListener(
                bleDevice,
                bleNotifyCallback,
                uuidService,
                uuidNotify,
                userCharacteristicDescriptor
            )
    }

    override fun connect(
        bleDevice: BluetoothLeDevice,
        connectCallback: IConnectCallback,
        connectTimeout: Long?
    ) {
        if (connectBeforeCheck(connectCallback)) {
            val gattConnectMirror = GattConnectMirror(bleDevice)
            gattConnectMirror.connect(bleDevice, connectCallback, connectTimeout)
        }
    }

    override fun connectByAddress(
        address: String,
        callback: IConnectCallback,
        connectTimeout: Long?,
        scanTimeout: Long?
    ) {
        BleManager.instance.bluetoothScan.startScan(ScanBluetoothCallback(object : IScanCallback {
            override fun onDeviceFound(bluetoothLeDevice: BluetoothLeDevice) {
                connect(bluetoothLeDevice, callback, connectTimeout)
            }

            override fun onScanFinish(devices: List<BluetoothLeDevice>) {

            }

            override fun onScanTimeout() {
                callback.onConnectFailure(
                    BleException(
                        BleExceptionCode.SCAN_ERROR,
                        "Bluetooth scan timeout"
                    )
                )
            }

            override fun onFailure(bleException: BleException) {
                callback.onConnectFailure(bleException)
            }

        }, object : IScanCallbackFilter {
            override fun onFilter(bluetoothLeDevice: BluetoothLeDevice): BluetoothLeDevice? {
                if (bluetoothLeDevice.getAddress() == address) {
                    BleScanCallbackManager.instance.stopScan()
                    return bluetoothLeDevice
                }
                return null
            }

        }), scanTimeout)
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

    override fun clear(bleDevice: BluetoothLeDevice) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)?.clear(bleDevice)
    }

    fun isConnected(bleDevice: BluetoothLeDevice): Boolean {
        return if (multipleGattBluetoothOpera.getGattConnectMirror(bleDevice) != null) {
            multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)!!.isConnected()
        } else {
            false
        }
    }

    override fun removeReadMessageListener(
        bleDevice: BluetoothLeDevice,
        bleIBleReadCallback: IBleReadCallback
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.removeReadMessageListener(bleDevice, bleIBleReadCallback)
    }

    override fun setMtu(
        bleDevice: BluetoothLeDevice,
        requiredMtu: Int,
        bleMtuChangedCallback: IBleGattMtuChangedCallback
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.setMtu(bleDevice, requiredMtu, bleMtuChangedCallback)
    }

    override fun getMtu(bleDevice: BluetoothLeDevice): Int {
        return if (multipleGattBluetoothOpera.getGattConnectMirror(bleDevice) != null) {
            multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)!!.getMtu(bleDevice)
        } else {
            BleConstant.DEFAULT_MTU_COUNT
        }
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice,
        heartContent: String,
        interval: Long,
        uuidService: String?,
        uuidWrite: String?
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.startHeartbeat(bleDevice, heartContent, interval, uuidService, uuidWrite)
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice,
        bytes: ByteArray,
        interval: Long,
        uuidService: String?,
        uuidWrite: String?
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.startHeartbeat(bleDevice, bytes, interval, uuidService, uuidWrite)
    }

    override fun addConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.addConnectStateListener(bleDevice, bleConnectStateListener)
    }

    override fun removeConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)
            ?.removeConnectStateListener(bleDevice, bleConnectStateListener)
    }

    fun disconnect(bleDevice: BluetoothLeDevice) {
        multipleGattBluetoothOpera.getGattConnectMirror(bleDevice)?.disconnect()
    }

    fun disconnectAllDevice() {
        multipleGattBluetoothOpera.disconnectAllDevice()
    }

    override fun getConnectedDeviceByAddress(address: String): BluetoothLeDevice? {
        return multipleGattBluetoothOpera.getConnectedDeviceByAddress(address)
    }
}
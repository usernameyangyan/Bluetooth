package com.example.bluetooth.manager

import com.example.bluetooth.manager.connect.gatt.GattConnectManager
import com.example.bluetooth.manager.connect.gatt.MultipleGattBluetoothOpera
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattMtuChangedCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattNotifyCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattRssiCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattWriteCallback
import com.example.bluetooth.manager.connect.gatt.impl.ICommonGattImpl
import com.example.bluetooth.manager.connect.impl.IAutoConnectImpl
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.connect.impl.IGetConnectedDeviceListener
import com.example.bluetooth.manager.connect.rfcomm.ICommonRfcommImpl
import com.example.bluetooth.manager.connect.rfcomm.MultipleRfcommBluetoothOpera
import com.example.bluetooth.manager.connect.rfcomm.RfcommConnectManager
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetooth.manager.scan.BleScanCallbackManager
import com.example.bluetooth.manager.scan.impl.ICommonScanImpl
import com.example.bluetooth.manager.scan.impl.ScanBluetoothCallback

/**
Create by yangyan
Create time:2023/8/29 14:34
Describe:蓝牙管理类
 */
class BleManager private constructor() {
    class Gatt private constructor() : ICommonGattImpl, IAutoConnectImpl,
        IGetConnectedDeviceListener {
        companion object {
            val instance: Gatt by lazy { Gatt() }
        }

        override fun writeMsg(
            bleDevice: BluetoothLeDevice,
            data: ByteArray?,
            bleWriteCallback: IBleGattWriteCallback?,
            uuidService: String?,
            uuidWrite: String?
        ) {
            GattConnectManager.instance.writeMsg(
                bleDevice,
                data,
                bleWriteCallback,
                uuidService,
                uuidWrite
            )
        }

        override fun addReadMessageListener(
            bleDevice: BluetoothLeDevice,
            bleReadCallback: IBleReadCallback,
            uuidService: String?,
            uuidRead: String?
        ) {
            GattConnectManager.instance.addReadMessageListener(
                bleDevice,
                bleReadCallback,
                uuidService,
                uuidRead
            )
        }

        override fun addRssiListener(
            bleDevice: BluetoothLeDevice,
            bleRssiCallback: IBleGattRssiCallback
        ) {
            GattConnectManager.instance.addRssiListener(bleDevice, bleRssiCallback)
        }

        override fun removeRssiListener(
            bleDevice: BluetoothLeDevice,
            bleRssiCallback: IBleGattRssiCallback
        ) {
            GattConnectManager.instance.removeRssiListener(bleDevice, bleRssiCallback)
        }

        override fun addNotifyMessageListener(
            bleDevice: BluetoothLeDevice,
            bleNotifyCallback: IBleGattNotifyCallback,
            uuidService: String?,
            uuidNotify: String?,
            userCharacteristicDescriptor: Boolean?
        ) {
            GattConnectManager.instance.addNotifyMessageListener(
                bleDevice,
                bleNotifyCallback,
                uuidService,
                uuidNotify,
                userCharacteristicDescriptor
            )
        }

        override fun removeNotifyMessageListener(
            bleDevice: BluetoothLeDevice,
            bleNotifyCallback: IBleGattNotifyCallback,
            uuidService: String?,
            uuidNotify: String?,
            userCharacteristicDescriptor: Boolean?
        ) {
            GattConnectManager.instance.removeNotifyMessageListener(
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
            GattConnectManager.instance.connect(bleDevice, connectCallback, connectTimeout)
        }

        override fun connectByAddress(
            address: String,
            callback: IConnectCallback,
            connectTimeout: Long?,
            scanTimeout: Long?
        ) {
            GattConnectManager.instance.connectByAddress(
                address,
                callback,
                connectTimeout,
                scanTimeout
            )
        }

        override fun clear(bleDevice: BluetoothLeDevice) {
            GattConnectManager.instance.clear(bleDevice)
        }


        override fun removeReadMessageListener(
            bleDevice: BluetoothLeDevice,
            bleIBleReadCallback: IBleReadCallback
        ) {
            GattConnectManager.instance.removeReadMessageListener(bleDevice, bleIBleReadCallback)
        }

        override fun setMtu(
            bleDevice: BluetoothLeDevice,
            requiredMtu: Int,
            bleMtuChangedCallback: IBleGattMtuChangedCallback
        ) {
            GattConnectManager.instance.setMtu(bleDevice, requiredMtu, bleMtuChangedCallback)
        }

        override fun getMtu(bleDevice: BluetoothLeDevice): Int {
            return GattConnectManager.instance.getMtu(bleDevice)
        }

        override fun startHeartbeat(
            bleDevice: BluetoothLeDevice,
            heartContent: String,
            interval: Long,
            uuidService: String?,
            uuidWrite: String?
        ) {
            GattConnectManager.instance.startHeartbeat(
                bleDevice,
                heartContent,
                interval,
                uuidService,
                uuidWrite
            )
        }

        override fun startHeartbeat(
            bleDevice: BluetoothLeDevice,
            bytes: ByteArray,
            interval: Long,
            uuidService: String?,
            uuidWrite: String?
        ) {
            GattConnectManager.instance.startHeartbeat(
                bleDevice,
                bytes,
                interval,
                uuidService,
                uuidWrite
            )
        }

        override fun addConnectStateListener(
            bleDevice: BluetoothLeDevice,
            bleConnectStateListener: IBleConnectStateListener
        ) {
            GattConnectManager.instance.addConnectStateListener(bleDevice, bleConnectStateListener)
        }

        override fun removeConnectStateListener(
            bleDevice: BluetoothLeDevice,
            bleConnectStateListener: IBleConnectStateListener
        ) {
            GattConnectManager.instance.removeConnectStateListener(
                bleDevice,
                bleConnectStateListener
            )
        }

        fun disconnect(bleDevice: BluetoothLeDevice) {
            GattConnectManager.instance.disconnect(bleDevice)
        }

        fun disconnectAllDevice() {
            GattConnectManager.instance.disconnectAllDevice()
        }

        fun getMultiGattOpera(): MultipleGattBluetoothOpera {
            return GattConnectManager.instance.getMultiGattOpera()
        }

        override fun getConnectedDeviceByAddress(address: String): BluetoothLeDevice? {
            return GattConnectManager.instance.getConnectedDeviceByAddress(address)
        }
    }

    class Rfcomm private constructor() : ICommonRfcommImpl, IAutoConnectImpl,
        IGetConnectedDeviceListener {
        companion object {
            val instance: Rfcomm by lazy { Rfcomm() }
        }

        override fun connect(
            bluetoothLeDevice: BluetoothLeDevice,
            connectCallback: IConnectCallback,
            connectTimeout: Long?
        ) {
            RfcommConnectManager.instance.connect(
                bluetoothLeDevice,
                connectCallback,
                connectTimeout
            )
        }

        override fun writeMsg(bleDevice: BluetoothLeDevice, msg: String): Boolean {
            return RfcommConnectManager.instance.writeMsg(bleDevice, msg)
        }

        override fun writeMsg(bleDevice: BluetoothLeDevice, bytes: ByteArray): Boolean {
            return RfcommConnectManager.instance.writeMsg(bleDevice, bytes)
        }

        override fun startHeartbeat(
            bleDevice: BluetoothLeDevice,
            heartContent: String,
            interval: Long
        ) {
            RfcommConnectManager.instance.startHeartbeat(bleDevice, heartContent, interval)
        }

        override fun startHeartbeat(
            bleDevice: BluetoothLeDevice,
            bytes: ByteArray,
            interval: Long
        ) {
            RfcommConnectManager.instance.startHeartbeat(bleDevice, bytes, interval)
        }

        override fun addMessageListener(
            bleDevice: BluetoothLeDevice,
            bleReadCallback: IBleReadCallback
        ) {
            RfcommConnectManager.instance.addMessageListener(bleDevice, bleReadCallback)
        }

        override fun removeMessageListener(
            bleDevice: BluetoothLeDevice,
            bleReadCallback: IBleReadCallback
        ) {
            RfcommConnectManager.instance.removeMessageListener(bleDevice, bleReadCallback)
        }

        override fun addConnectStateListener(
            bleDevice: BluetoothLeDevice,
            bleConnectStateListener: IBleConnectStateListener
        ) {
            RfcommConnectManager.instance.addConnectStateListener(
                bleDevice,
                bleConnectStateListener
            )
        }

        override fun removeConnectStateListener(
            bleDevice: BluetoothLeDevice,
            bleConnectStateListener: IBleConnectStateListener
        ) {
            RfcommConnectManager.instance.removeConnectStateListener(
                bleDevice,
                bleConnectStateListener
            )
        }

        fun disconnect(bleDevice: BluetoothLeDevice) {
            RfcommConnectManager.instance.disconnect(bleDevice)
        }

        fun disconnectAllDevice() {
            RfcommConnectManager.instance.disconnectAllDevice()
        }

        fun getMultiRfcommOpera(): MultipleRfcommBluetoothOpera {
            return RfcommConnectManager.instance.getMultiRfcommOpera()
        }

        override fun connectByAddress(
            address: String,
            callback: IConnectCallback,
            connectTimeout: Long?,
            scanTimeout: Long?
        ) {
            RfcommConnectManager.instance.connectByAddress(
                address,
                callback,
                connectTimeout,
                scanTimeout
            )
        }

        override fun getConnectedDeviceByAddress(address: String): BluetoothLeDevice? {
            return RfcommConnectManager.instance.getConnectedDeviceByAddress(address)
        }
    }

    class BluetoothScan private constructor() : ICommonScanImpl {
        companion object {
            val instance: BluetoothScan by lazy { BluetoothScan() }
        }

        override fun stopScan(isClear: Boolean) {
            BleScanCallbackManager.instance.stopScan(isClear)
        }

        override fun startScan(scanCallback: ScanBluetoothCallback, scanTimeout: Long?) {
            BleScanCallbackManager.instance.startScan(scanCallback, scanTimeout)
        }
    }
}
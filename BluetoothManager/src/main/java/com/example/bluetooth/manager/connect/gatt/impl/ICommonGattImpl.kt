package com.example.bluetooth.manager.connect.gatt.impl
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.model.BluetoothLeDevice
/**
Create by yangyan
Create time:2023/9/2 00:47
Describe:gatt 公用接口抽离
 */
interface ICommonGattImpl {
    fun connect(bleDevice: BluetoothLeDevice, connectCallback: IConnectCallback, connectTimeout: Long? = null)
    fun writeMsg(
        bleDevice: BluetoothLeDevice,
        data: ByteArray?,
        bleWriteCallback: IBleGattWriteCallback?,
        uuidService: String?=null,
        uuidWrite: String?=null
    )

    fun addReadMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback,
        uuidService: String?=null,
        uuidRead: String?=null
    )
    fun removeReadMessageListener(bleDevice: BluetoothLeDevice,bleIBleReadCallback: IBleReadCallback)
    fun addRssiListener(bleDevice: BluetoothLeDevice,bleRssiCallback: IBleGattRssiCallback)
    fun removeRssiListener(bleDevice: BluetoothLeDevice,bleRssiCallback: IBleGattRssiCallback)

    fun addNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?=null,
        uuidNotify: String?=null,
        userCharacteristicDescriptor: Boolean?=true
    )
    fun removeNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?=null,
        uuidNotify: String?=null,
        userCharacteristicDescriptor: Boolean?=true)
    fun clear(bleDevice: BluetoothLeDevice)
    fun setMtu(bleDevice: BluetoothLeDevice,requiredMtu: Int, bleMtuChangedCallback: IBleGattMtuChangedCallback)
    fun getMtu(bleDevice: BluetoothLeDevice):Int
    fun startHeartbeat(bleDevice: BluetoothLeDevice,heartContent: String, interval: Long,uuidService: String?=null,
                       uuidWrite: String?=null)
    fun startHeartbeat(bleDevice: BluetoothLeDevice,bytes: ByteArray, interval: Long,uuidService: String?=null,
                       uuidWrite: String?=null)

    fun addConnectStateListener(bleDevice: BluetoothLeDevice,bleConnectStateListener: IBleConnectStateListener)
    fun removeConnectStateListener(bleDevice: BluetoothLeDevice,bleConnectStateListener: IBleConnectStateListener)

}
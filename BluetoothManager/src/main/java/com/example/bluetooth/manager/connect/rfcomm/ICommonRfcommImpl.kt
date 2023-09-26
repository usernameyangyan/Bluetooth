package com.example.bluetooth.manager.connect.rfcomm

import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.model.BluetoothLeDevice

/**
Create by yangyan
Create time:2023/8/31 14:43
Describe:rfcomm 公共接口抽离
 */
interface ICommonRfcommImpl {
    /**
     * @param msg 发送内容
     */
    fun writeMsg(bleDevice: BluetoothLeDevice,msg: String):Boolean
    /**
     * @param bytes 发送内容
     */
    fun writeMsg(bleDevice: BluetoothLeDevice,bytes: ByteArray):Boolean
    /**
     * @param heartContent 心跳内容
     * @param interval 心跳发送间隔
     */
    fun startHeartbeat(bleDevice: BluetoothLeDevice,heartContent: String, interval: Long)
    /**
     * @param bytes 心跳内容
     * @param interval 心跳发送间隔
     */
    fun startHeartbeat(bleDevice: BluetoothLeDevice,bytes: ByteArray, interval: Long)
    fun connect(
        bluetoothLeDevice: BluetoothLeDevice,
        connectCallback: IConnectCallback,
        connectTimeout: Long? = null
    )

    fun addMessageListener(bleDevice: BluetoothLeDevice,bleReadCallback: IBleReadCallback)
    fun removeMessageListener(bleDevice: BluetoothLeDevice,bleReadCallback: IBleReadCallback)
    fun addConnectStateListener(bleDevice: BluetoothLeDevice,bleConnectStateListener: IBleConnectStateListener)
    fun removeConnectStateListener(bleDevice: BluetoothLeDevice,bleConnectStateListener: IBleConnectStateListener)
}
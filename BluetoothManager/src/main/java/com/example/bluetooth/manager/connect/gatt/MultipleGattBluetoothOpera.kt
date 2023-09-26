package com.example.bluetooth.manager.connect.gatt

import android.annotation.SuppressLint
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.connect.impl.IGetConnectedDeviceListener
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetooth.manager.utils.BleLruHashMap

/**
Create by yangyan
Create time:2023/9/1 11:59
Describe:多设备连接管理
 */
class MultipleGattBluetoothOpera : IGetConnectedDeviceListener {
    private val maxConnectCount: Int =BleConfig.instance.getMaxConnectCount()
    private var bleLruHashMap=BleLruHashMap<String, GattConnectMirror>(maxConnectCount)

    @Synchronized
    fun addGattConnectMirror(gattConnectMirror: GattConnectMirror) {
        refreshConnectedDevice()
        if (!bleLruHashMap.containsKey(gattConnectMirror.getDeviceUniqueKey())) {
            bleLruHashMap[gattConnectMirror.getDeviceUniqueKey()] = gattConnectMirror
        }
    }

    @Synchronized
    fun removeGattConnectMirror(gattConnectMirror: GattConnectMirror?) {
        gattConnectMirror?.let {
            if (bleLruHashMap.containsKey(it.getDeviceUniqueKey())) {
                bleLruHashMap.remove(it.getDeviceUniqueKey())
            }
        }
    }


    @Synchronized
    fun getGattConnectMirror(bleDevice: BluetoothLeDevice?): GattConnectMirror? {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.getDeviceUniqueKey())) {
                return bleLruHashMap[bleDevice.getDeviceUniqueKey()]
            }
        }
        return null
    }


    @SuppressLint("MissingPermission")
    @Synchronized
    fun isContainConnectDevice(bluetoothDevice: BluetoothLeDevice?): Boolean {
        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.getDeviceUniqueKey())
    }


    @Synchronized
    fun disconnect(bleDevice: BluetoothLeDevice?) {
        if (isContainConnectDevice(bleDevice)) {
            getGattConnectMirror(bleDevice)?.disconnect()
        }
    }

    @Synchronized
    fun disconnectAllDevice() {
        for(value in bleLruHashMap.values){
            value.disconnect()
        }
        bleLruHashMap.clear()
    }

    @Synchronized
    fun getGattConnectMirrorList(): List<GattConnectMirror> {
        return ArrayList<GattConnectMirror>(
            bleLruHashMap.values
        )
    }

    @Synchronized
    fun getConnectedBluetoothDeviceList(): ArrayList<BluetoothLeDevice> {
        refreshConnectedDevice()
        val deviceList = ArrayList<BluetoothLeDevice>()
        for (connectMirror in getGattConnectMirrorList()) {
            deviceList.add(connectMirror.getBleDevice())
        }
        return deviceList
    }
    private fun refreshConnectedDevice() {
        val bluetoothList: List<GattConnectMirror> = getGattConnectMirrorList()
        bluetoothList.forEach {
            if (!it.isConnected()) {
                removeGattConnectMirror(it)
            }
        }
    }

    override fun getConnectedDeviceByAddress(address:String):BluetoothLeDevice?{
        for(mirror in bleLruHashMap.values){
            if(mirror.getBleDevice().getAddress()==address){
                return mirror.getBleDevice()
            }
        }
        return null
    }
}
package com.example.bluetooth.manager.model
/**
Create by yangyan
Create time:2023/8/29 11:41
Describe:
 */
class BluetoothLeDeviceResults private constructor(){
    companion object {
        val instance: BluetoothLeDeviceResults by lazy { BluetoothLeDeviceResults() }
    }
    private var mDeviceMap=HashMap<String, BluetoothLeDevice>()
    fun addDevice(device: BluetoothLeDevice) {
        mDeviceMap[device.getDeviceUniqueKey()] = device
    }
    fun removeDevice(device: BluetoothLeDevice) {
        if (mDeviceMap.containsKey(device.getAddress())) {
            mDeviceMap.remove(device.getAddress())
        }
    }
    fun clear() {
        mDeviceMap.clear()
    }
    fun getDeviceList(): List<BluetoothLeDevice> {
        return ArrayList(mDeviceMap.values)
    }
    fun isContainDevice(device: BluetoothLeDevice):Boolean{
        return mDeviceMap.containsKey(device.getDeviceUniqueKey())
    }
}
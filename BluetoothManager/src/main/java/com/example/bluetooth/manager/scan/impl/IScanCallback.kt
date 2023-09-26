package com.example.bluetooth.manager.scan.impl
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.model.BluetoothLeDevice

/**
Create by yangyan
Create time:2023/8/29 11:38
Describe:扫码回调
 */
interface IScanCallback {
    fun onDeviceFound(bluetoothLeDevice: BluetoothLeDevice)
    fun onScanFinish(devices:List<BluetoothLeDevice>)
    fun onScanTimeout()
    fun onFailure(bleException: BleException)

}
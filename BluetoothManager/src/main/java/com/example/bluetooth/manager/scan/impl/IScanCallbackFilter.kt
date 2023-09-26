package com.example.bluetooth.manager.scan.impl
import com.example.bluetooth.manager.model.BluetoothLeDevice

/**
Create by yangyan
Create time:2023/8/29 14:16
Describe:扫描过滤
 */
interface IScanCallbackFilter {
    fun onFilter(bluetoothLeDevice: BluetoothLeDevice): BluetoothLeDevice?
}
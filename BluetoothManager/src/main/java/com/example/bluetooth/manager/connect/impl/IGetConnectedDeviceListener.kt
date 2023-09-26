package com.example.bluetooth.manager.connect.impl

import com.example.bluetooth.manager.model.BluetoothLeDevice

/**
Create by yangyan
Create time:2023/9/4 09:25
Describe:
 */
interface IGetConnectedDeviceListener {
    fun getConnectedDeviceByAddress(address:String): BluetoothLeDevice?
}
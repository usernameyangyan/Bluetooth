package com.example.bluetooth.manager.connect.gatt.impl

import com.example.bluetooth.manager.exception.BleException

/**
Create by yangyan
Create time:2023/9/1 11:35
Describe:gatt 信号强度设置回调
 */
interface IBleGattRssiCallback {
    fun onRssiFailure(exception: BleException)
    fun onRssiSuccess(rssi: Int)
}
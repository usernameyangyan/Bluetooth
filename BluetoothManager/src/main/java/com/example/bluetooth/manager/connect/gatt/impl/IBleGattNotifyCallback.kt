package com.example.bluetooth.manager.connect.gatt.impl

import com.example.bluetooth.manager.exception.BleException

/**
Create by yangyan
Create time:2023/9/1 10:15
Describe:gatt 通知设置回调
 */
interface IBleGattNotifyCallback {
    fun onNotifySuccess()
    fun onNotifyFailure(exception: BleException)
    fun onCharacteristicChanged(data: ByteArray)
}
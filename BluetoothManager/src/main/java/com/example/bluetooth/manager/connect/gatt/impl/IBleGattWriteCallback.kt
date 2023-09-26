package com.example.bluetooth.manager.connect.gatt.impl

import com.example.bluetooth.manager.exception.BleException

/**
Create by yangyan
Create time:2023/9/1 10:13
Describe:gatt 写操作设置回调
 */
interface IBleGattWriteCallback {
    fun onWriteSuccess(justWrite: ByteArray?)
    fun onWriteFailure(exception: BleException)
}
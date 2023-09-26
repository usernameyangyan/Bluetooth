package com.example.bluetooth.manager.connect.impl

import com.example.bluetooth.manager.exception.BleException

/**
Create by yangyan
Create time:2023/9/1 10:14
Describe:消息读取
 */
interface IBleReadCallback {
    fun onReadSuccess(data: ByteArray)
    fun onReadFailure(exception: BleException)
}
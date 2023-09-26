package com.example.bluetooth.manager.connect.gatt.impl

import com.example.bluetooth.manager.exception.BleException

/**
Create by yangyan
Create time:2023/9/2 08:48
Describe:gatt MTU设置回调
 */
interface IBleGattMtuChangedCallback {
    fun onSetMTUFailure(exception: BleException)
    fun onMtuChanged(mtu: Int)
}
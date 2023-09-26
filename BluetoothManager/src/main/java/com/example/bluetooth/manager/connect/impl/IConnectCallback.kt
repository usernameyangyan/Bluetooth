package com.example.bluetooth.manager.connect.impl

import android.bluetooth.BluetoothGatt
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.model.BluetoothLeDevice

/**
Create by yangyan
Create time:2023/8/31 09:55
Describe:连接回调
 */
interface IConnectCallback {
    fun onConnectFailure(exception: BleException)
    fun onConnectSuccess(bleDevice:BluetoothLeDevice,bleGatt:BluetoothGatt?=null)
    fun onPairing(){
    }
    fun onPairFailure(exception: BleException){
    }
    fun onPairSuccess(){
    }
}
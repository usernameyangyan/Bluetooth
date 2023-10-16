package com.example.bluetooth.manager.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.android.parcel.Parcelize

/**
Create by yangyan
Create time:2023/8/29 16:17
Describe:
 */
@Parcelize
data class BluetoothLeDevice(val device: BluetoothDevice, val rssi:Int,val scanRecord:ByteArray?, val timestamp:Long):Parcelable{
    fun getAddress():String{
        return device.address
    }
    @SuppressLint("MissingPermission")
    fun getDeviceName():String?{
        return device.name
    }

    fun getDeviceUniqueKey():String{
        return device.address+getDeviceName()
    }
}

package com.example.bluetooth.manager.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager

/**
Create by yangyan
Create time:2023/8/31 16:07
Describe:蓝牙工具类
 */
object BleUtils {
    fun isSupportBle(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false
        }
        return getBluetoothManagerAdapter(context) != null
    }

    fun isBleEnable(context: Context): Boolean {
        if (!isSupportBle(context)) {
            return false
        }
        return getBluetoothManagerAdapter(context)!!.isEnabled
    }

    fun getBluetoothManagerAdapter(context: Context):BluetoothAdapter?{
        return getBluetoothManager(context).adapter
    }

    fun getBluetoothManager(context: Context): BluetoothManager {
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

}
package com.example.bluetooth.manager.scan.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.example.bluetooth.manager.model.BluetoothLeDevice
import com.example.bluetooth.manager.model.BluetoothLeDeviceResults

/**
Create by yangyan
Create time:2023/8/29 14:20
Describe:蓝牙扫描实现
 */
open class ScanBluetoothCallback(private var scanCallback: IScanCallback,private var scanCallbackFilter:IScanCallbackFilter?=null):BluetoothAdapter.LeScanCallback {
    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
        if(!isFreezePeriod){
            dealDevice(device,rssi,scanRecord)
        }
    }
    fun dealDevice(device: BluetoothDevice,rssi:Int?,scanRecord:ByteArray?){
        val bluetoothLeDevice = BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis())
        val isContain=BluetoothLeDeviceResults.instance.isContainDevice(bluetoothLeDevice)
        if(!isContain){
            val filterDevice = if(scanCallbackFilter!=null){
                scanCallbackFilter!!.onFilter(bluetoothLeDevice)
            }else{
                bluetoothLeDevice
            }
            filterDevice?.let {
                BluetoothLeDeviceResults.instance.addDevice(it)
                scanCallback.onDeviceFound(it)
            }
        }
    }
    fun getScanCallback():IScanCallback{
        return scanCallback
    }
    private var isFreezePeriod=false
    fun setIsFreezePeriod(isFreezePeriod:Boolean){
        this.isFreezePeriod=isFreezePeriod
    }
}
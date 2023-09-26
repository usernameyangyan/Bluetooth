package com.example.bluetooth.manager.scan.impl

/**
Create by yangyan
Create time:2023/9/2 18:32
Describe:蓝牙扫描接口抽离
 */
interface ICommonScanImpl {
    fun stopScan(isClear: Boolean? = true)
    fun startScan(scanCallback: ScanBluetoothCallback, scanTimeout: Long? = null)
}
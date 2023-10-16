package com.example.bluetooth.manager.scan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.model.BluetoothLeDeviceResults
import com.example.bluetooth.manager.scan.impl.ICommonScanImpl
import com.example.bluetooth.manager.scan.impl.ScanBluetoothCallback
import com.example.bluetooth.manager.utils.BleUtils
import java.lang.StringBuilder

/**
Create by yangyan
Create time:2023/8/29 14:29
Describe:蓝牙扫描管理类
 */
class BleScanCallbackManager private constructor() :ICommonScanImpl{
    companion object {
        val instance: BleScanCallbackManager by lazy { BleScanCallbackManager() }
    }

    private var handler = Looper.myLooper()?.let { Handler(it) }
    private var isScanning = false //是否正在扫描
    private var scanCallback: ScanBluetoothCallback? = null

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
    )

    private val PERMISSIONS1 = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @SuppressLint("MissingPermission")
    override fun stopScan(isClear: Boolean) {
        if(isClear){
            removeHandlerMsg()
        }
        if(BleConfig.instance.getBleScanType()===BleScanType.SCAN_LOW_POWER){
            scanCallback?.let {
                BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.stopLeScan(it)
            }
        }else{
            BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.cancelDiscovery()
        }
        isScanning = false
    }
    @SuppressLint("MissingPermission")
    override fun startScan(scanCallback: ScanBluetoothCallback, scanTimeout: Long?) {
        if(!BleUtils.isBleEnable(BleConfig.instance.getContext())){
            scanCallback.getScanCallback().onFailure(BleException(BleExceptionCode.SCAN_ERROR,"Bluetooth not enable!"))
            return
        }
        val checkPermissionMsg=StringBuilder()
        val permissions=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PERMISSIONS else PERMISSIONS1

        var index=0
        for (permission in permissions){
            if(ContextCompat.checkSelfPermission(BleConfig.instance.getContext(),permission)!= PackageManager.PERMISSION_GRANTED){
                if(index==0){
                    checkPermissionMsg.append(permission)
                }else{
                    checkPermissionMsg.append(",${permission}")
                }
                index++
            }
        }

        if(checkPermissionMsg.toString().isNotEmpty()){
            scanCallback.getScanCallback().onFailure(BleException(BleExceptionCode.SCAN_ERROR,"Bluetooth scan need permissions:${checkPermissionMsg}"))
            return
        }
        if (isScanning) {
            return
        }
        this.scanCallback = scanCallback
        BluetoothLeDeviceResults.instance.clear()
        val timeoutScan = scanTimeout ?: BleConfig.instance.getScanTimeout()
        if (timeoutScan > 0) {
            handler?.postDelayed({
                scanCycle(isRemoveHandleMsg = true, isScanRepeat = false)
            }, timeoutScan)
        } else if (BleConfig.instance.getScanRepeatInterval() > 0) {
            //如果超时时间设置为一直扫描（即 <= 0）,则判断是否设置重复扫描间隔
            handler?.postDelayed(object : Runnable {
                override fun run() {
                    scanCycle(isRemoveHandleMsg = false, isScanRepeat = true)
                    isScanning = true
                    if(BleConfig.instance.getBleScanType()===BleScanType.SCAN_LOW_POWER){
                        BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.startLeScan(scanCallback)
                    }else{
                        BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.startDiscovery()
                    }
                    handler?.postDelayed(this, BleConfig.instance.getScanRepeatInterval())
                }
            }, BleConfig.instance.getScanRepeatInterval())
        }
        isScanning = true
        if(BleConfig.instance.getBleScanType()===BleScanType.SCAN_LOW_POWER){
            BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.startLeScan(scanCallback)
        }else{
            initDiscoveryFilter()
            BleUtils.getBluetoothManagerAdapter(BleConfig.instance.getContext())?.startDiscovery()
        }

    }

    private var isFreezePeriod=false
    @SuppressLint("MissingPermission")
    private fun scanCycle(isRemoveHandleMsg: Boolean,isScanRepeat:Boolean) {
        isFreezePeriod=true
        scanCallback?.setIsFreezePeriod(true)
        scanCallback?.getScanCallback()
            ?.onScanFinish(BluetoothLeDeviceResults.instance.getDeviceList())
        if(!isScanRepeat){
            scanCallback?.getScanCallback()?.onScanTimeout()
        }
        stopScan(isRemoveHandleMsg)
        scanCallback?.setIsFreezePeriod(false)
        isFreezePeriod=false
    }

    private var discoveryFilter:IntentFilter?=null
    private fun initDiscoveryFilter(){
        if(discoveryFilter==null){
            discoveryFilter = IntentFilter()
            discoveryFilter?.addAction(BluetoothDevice.ACTION_FOUND)
            BleConfig.instance.getContext().registerReceiver(receiver, discoveryFilter)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("NewApi")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action&&!isFreezePeriod) {
                // 处理发现的设备
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                device?.let {
                    scanCallback?.dealDevice(device,0,null)
                }
            }
        }
    }

    private fun removeHandlerMsg() {
        handler?.removeCallbacksAndMessages(null)
        BluetoothLeDeviceResults.instance.clear()
        if(BleConfig.instance.getBleScanType()===BleScanType.SCAN_DISCOVERY){
            BleConfig.instance.getContext().unregisterReceiver(receiver)
        }
    }

}
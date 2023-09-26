package com.example.bluetooth.manager.common
import android.content.Context
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_CONN_TIME
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_MAX_CONNECT_COUNT
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_PAIR
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_SCAN_REPEAT_INTERVAL
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_SCAN_TIME
import com.example.bluetooth.manager.common.BleConstant.DEFAULT_UUID_SPP
import com.example.bluetooth.manager.scan.BleScanType
import java.util.UUID

/**
Create by yangyan
Create time:2023/8/29 14:43
Describe:蓝牙配置文件
 */
class BleConfig private constructor(){
    companion object {
        val instance: BleConfig by lazy { BleConfig() }
    }

    private var scanTimeout: Long = DEFAULT_SCAN_TIME //扫描超时时间（毫秒）
    private var scanRepeatInterval: Long = DEFAULT_SCAN_REPEAT_INTERVAL //每隔X时间重复扫描 (毫秒)
    private var connectTimeout: Long = DEFAULT_CONN_TIME //连接超时时间（毫秒）
    private var uuidSPP: UUID = UUID.fromString(DEFAULT_UUID_SPP) //SPP uuid
    private var mergeWholeMsgRule:MergeWholeMsgRule?=null
    private lateinit var context:Context
    private var isGattAutoConnect:Boolean=false//gatt自动连接
    private var maxConnectCount: Int = DEFAULT_MAX_CONNECT_COUNT //最大连接数量
    private var scanBluetoothType:BleScanType=BleScanType.SCAN_LOW_POWER
    private var isNeedPair=DEFAULT_PAIR


    /**
     * 获取扫描超时时间
     *
     * @return 返回扫描超时时间
     */
    fun getScanTimeout(): Long {
        return scanTimeout
    }

    /**
     * 设置扫描超时时间
     *
     * @param scanTimeout 扫描超时时间
     * @return 返回ViseBle
     */
    fun setScanTimeout(scanTimeout: Long): BleConfig {
        this.scanTimeout = scanTimeout
        return this
    }

    /**
     * 获取扫描间隔时间
     * @return
     */
    fun getScanRepeatInterval(): Long {
        return scanRepeatInterval
    }

    /**
     * 设置每隔多少时间重复扫描一次
     * 设置扫描间隔时间 （毫秒）
     * @param scanRepeatInterval
     * @return
     */
    fun setScanRepeatInterval(scanRepeatInterval: Long): BleConfig {
        this.scanRepeatInterval = scanRepeatInterval
        return this
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间
     * @return 返回ViseBle
     */
    fun setConnectTimeout(connectTimeout: Long): BleConfig {
        this.connectTimeout = connectTimeout
        return this
    }

    /**
     * 获取连接超时时间
     *
     * @return 返回连接超时时间
     */
    fun getConnectTimeout(): Long {
        return connectTimeout
    }

    /**
     * 获取UUID_SPP
     * @return
     */
    fun getUUIDBySSP(): UUID {
        return uuidSPP
    }

    /**
     * 设置连接重试次数
     *
     * @param uuid
     * @return
     */
    fun setUUidToSPP(uuid:String): BleConfig {
        this.uuidSPP = UUID.fromString(uuid)
        return this
    }

    /**
     * @param mergeWholeMsgRule 消息内容合并规则
     */
    fun setMergeWholeMsgRule(mergeWholeMsgRule:MergeWholeMsgRule):BleConfig{
        this.mergeWholeMsgRule=mergeWholeMsgRule
        return this
    }

    /**
     * 获取消息内容合并规则
     */
    fun getMergeWholeMsgRule():MergeWholeMsgRule?{
        return mergeWholeMsgRule
    }

    /**
     * 设置Gatt是否自动连接
     */
    fun setGattAutoConnect(isAutoConnect:Boolean) {
        this.isGattAutoConnect = isAutoConnect
    }
    /**
     * 获取Gatt是否自动连接
     */
    fun getGattAutoConnect():Boolean{
        return isGattAutoConnect
    }


    /**
     * 获取最大连接数量
     *
     * @return
     */
    fun getMaxConnectCount(): Int {
        return maxConnectCount
    }

    /**
     * 设置最大连接数量
     *
     * @param maxConnectCount
     * @return
     */
    fun setMaxConnectCount(maxConnectCount: Int): BleConfig {
        this.maxConnectCount = maxConnectCount
        return this
    }

    /**
     * 获取蓝牙扫描方式
     *
     * @return
     */
    fun getBleScanType(): BleScanType {
        return scanBluetoothType
    }

    /**
     * 设置蓝牙扫描方式
     *
     * @param bleScanType
     * @return
     */
    fun setBleScanType(bleScanType: BleScanType): BleConfig {
        this.scanBluetoothType = bleScanType
        return this
    }
    /**
     * 获取蓝牙蓝牙是否需要配对
     *
     * @return
     */
    fun getIsNeedPair(): Boolean {
        return isNeedPair
    }

    /**
     * 设置蓝牙扫描方式
     *
     * @param isNeedPair
     * @return
     */
    fun setIsNeedPair(isNeedPair: Boolean): BleConfig {
        this.isNeedPair = isNeedPair
        return this
    }
    /**
     * 完成初始化
     */
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    /**
     * 获取Context
     */
    fun getContext():Context{
        return context
    }
}
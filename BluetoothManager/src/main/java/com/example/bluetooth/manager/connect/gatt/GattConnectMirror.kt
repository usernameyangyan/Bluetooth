package com.example.bluetooth.manager.connect.gatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.common.BleConstant
import com.example.bluetooth.manager.common.IDeviceConnectMirror
import com.example.bluetooth.manager.common.MessageDeal
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattMtuChangedCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattNotifyCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattRssiCallback
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattWriteCallback
import com.example.bluetooth.manager.connect.gatt.impl.ICommonGattImpl
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.model.BluetoothLeDevice
import java.util.UUID

/**
Create by yangyan
Create time:2023/9/1 09:23
Describe:gatt设备镜像
 */
class GattConnectMirror(private val bluetoothBleDevice: BluetoothLeDevice) : IDeviceConnectMirror,
    ICommonGattImpl {
    private inner class GattHandler constructor(looper: Looper) : Handler(looper) {
        @SuppressLint("MissingPermission")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BleConstant.MSG_CONNECT_TIMEOUT -> {
                    connectCallback?.onConnectFailure(
                        BleException(
                            BleExceptionCode.TIMEOUT,
                            "GATT connect timeout!"
                        )
                    )
                    clearBluetoothState()
                }

                BleConstant.MSG_DIS_CONNECT -> {
                    if (isConnected) {
                        disconnect()
                    } else {
                        if (!isStopConnect) {
                            isConnecting = false
                            connect()
                        }
                    }
                }

                BleConstant.MSG_DISCOVER_FAIL -> {
                    clearBluetoothState()
                    connectCallback?.onConnectFailure(
                        BleException(
                            BleExceptionCode.OTHER_FAIL,
                            "GATT discover services exception occurred!"
                        )
                    )
                }

                BleConstant.MSG_DISCOVER_SUCCESS -> {
                    GattConnectManager.instance.getMultiGattOpera()
                        .addGattConnectMirror(this@GattConnectMirror)
                    isConnected = true
                    sendConnectState()
                    isConnecting = false
                    connectCallback?.onConnectSuccess(bluetoothBleDevice, bluetoothGatt)
                }

                BleConstant.MSG_CHAR_READ_RESULT -> {
                    val bundle = msg.data
                    val status = bundle.getInt(BleConstant.KEY_READ_BUNDLE_STATUS)
                    val value = bundle.getByteArray(BleConstant.KEY_READ_BUNDLE_VALUE)
                    val readCallback = msg.obj as IBleReadCallback
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val msgValue = if (BleConfig.instance.getMergeWholeMsgRule() != null) {
                            messageDeal.convertMessage(value)
                        } else {
                            value
                        }
                        msgValue?.let {
                            readCallback.onReadSuccess(it)
                        }
                    } else {
                        readCallback.onReadFailure(
                            BleException(
                                BleExceptionCode.GATT_ERROR,
                                "Gatt Exception Occurred!,read status:${status}"
                            )
                        )
                    }
                }

                BleConstant.MSG_NOTIFY_DATA_CHANGE -> {
                    val bundle = msg.data
                    val value = bundle.getByteArray(BleConstant.KEY_NOTIFY_BUNDLE_VALUE)
                    val notifyCallback = msg.obj as IBleGattNotifyCallback
                    val msgValue = if (BleConfig.instance.getMergeWholeMsgRule() != null) {
                        messageDeal.convertMessage(value)
                    } else {
                        value
                    }
                    msgValue?.let {
                        notifyCallback.onCharacteristicChanged(it)
                    }

                }

                BleConstant.MSG_READ_RSSI_RESULT -> {
                    val rssiCallback = msg.obj as IBleGattRssiCallback
                    val bundle = msg.data
                    val status = bundle.getInt(BleConstant.KEY_READ_RSSI_BUNDLE_STATUS)
                    val value = bundle.getInt(BleConstant.KEY_READ_RSSI_BUNDLE_VALUE)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        rssiCallback.onRssiSuccess(value)
                    } else {
                        rssiCallback.onRssiFailure(
                            BleException(
                                BleExceptionCode.GATT_ERROR,
                                "Gatt Exception Occurred!,write status:${status}"
                            )
                        )
                    }
                }

                BleConstant.MSG_CHA_NOTIFY_RESULT -> {
                    val notifyCallback = msg.obj as IBleGattNotifyCallback
                    val bundle = msg.data
                    val status = bundle.getInt(BleConstant.KEY_NOTIFY_BUNDLE_STATUS)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        notifyCallback.onNotifySuccess()
                    } else {
                        notifyCallback.onNotifyFailure(
                            BleException(
                                BleExceptionCode.GATT_ERROR,
                                "Gatt Exception Occurred!,Notify status:${status}"
                            )
                        )
                    }
                }

                BleConstant.MSG_SET_MTU_RESULT -> {
                    val mtuChangedCallback = msg.obj as IBleGattMtuChangedCallback
                    val bundle = msg.data
                    val status = bundle.getInt(BleConstant.KEY_SET_MTU_BUNDLE_STATUS)
                    val value = bundle.getInt(BleConstant.KEY_SET_MTU_BUNDLE_VALUE)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mtuChangedCallback.onMtuChanged(value)
                    } else {
                        mtuChangedCallback.onSetMTUFailure(
                            BleException(
                                BleExceptionCode.GATT_ERROR,
                                "Gatt Exception Occurred!,Mtu status:${status}"
                            )
                        )
                    }
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        /**
         * 连接状态改变，主要用来分析设备的连接与断开
         * @param gatt GATT
         * @param status 改变前状态
         * @param newState 改变后状态
         */
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            bluetoothGatt = gatt
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {//做重连
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_DIS_CONNECT
                    it.sendMessage(message)
                }
            }
        }

        /**
         * 发现服务，主要用来获取设备支持的服务列表
         * @param gatt GATT
         * @param status 当前状态
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            handler?.removeMessages(BleConstant.MSG_CONNECT_TIMEOUT)
            isStopConnect = true
            if (status == 0) {
                bluetoothGatt = gatt
                bluetoothGattChannel.initCharacteristic()
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_DISCOVER_SUCCESS
                    it.sendMessage(message)
                }
            } else {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_DISCOVER_FAIL
                    it.sendMessage(message)
                }
            }
        }

        /**
         * 读取特征值，主要用来读取该特征值包含的可读信息
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            for (value in bleReadCallbackList) {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_CHAR_READ_RESULT
                    message.obj = value
                    val bundle = Bundle()
                    bundle.putInt(BleConstant.KEY_READ_BUNDLE_STATUS, status)
                    bundle.putByteArray(BleConstant.KEY_READ_BUNDLE_VALUE, characteristic.value)
                    message.data = bundle
                    it.sendMessage(message)
                }
            }

        }

        /**
         * 特征值改变，主要用来接收设备返回的数据信息
         * @param gatt GATT
         * @param characteristic 特征值
         */
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            for (value in bleNotifyCallbackList) {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_NOTIFY_DATA_CHANGE
                    message.obj = value
                    val bundle = Bundle()
                    bundle.putByteArray(BleConstant.KEY_NOTIFY_BUNDLE_VALUE, characteristic.value)
                    message.data = bundle
                    it.sendMessage(message)
                }
            }
        }

        /**
         * 阅读设备信号值
         * @param gatt GATT
         * @param rssi 设备当前信号
         * @param status 当前状态
         */
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            for (value in bleRssiCallbackList) {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_READ_RSSI_RESULT
                    message.obj = value
                    val bundle = Bundle()
                    bundle.putInt(BleConstant.KEY_READ_RSSI_BUNDLE_STATUS, status)
                    bundle.putInt(BleConstant.KEY_READ_RSSI_BUNDLE_VALUE, rssi)
                    message.data = bundle
                    it.sendMessage(message)
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            for (value in bleNotifyCallbackList) {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_CHA_NOTIFY_RESULT
                    message.obj = value
                    val bundle = Bundle()
                    bundle.putInt(BleConstant.KEY_NOTIFY_BUNDLE_STATUS, status)
                    message.data = bundle
                    it.sendMessage(message)
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            mtuCount = mtu
            if (bleMtuChangedCallback != null) {
                handler?.let {
                    val message = it.obtainMessage()
                    message.what = BleConstant.MSG_SET_MTU_RESULT
                    message.obj = bleMtuChangedCallback
                    val bundle = Bundle()
                    bundle.putInt(BleConstant.KEY_SET_MTU_BUNDLE_STATUS, status)
                    bundle.putInt(BleConstant.KEY_SET_MTU_BUNDLE_VALUE, mtu)
                    message.data = bundle
                    it.sendMessage(message)
                }
            }
        }
    }

    private fun sendConnectState() {
        if (isPreConnected != isConnected) {
            for (bleConnectStateListener in bleConnectStateListenerList) {
                bleConnectStateListener.onConnectState(isConnected)
            }
            isPreConnected = isConnected
        }
    }

    //设置默认UUID
    private inner class BluetoothGattChannel {
        private val writeMap = HashMap<UUID, BluetoothGattCharacteristic>()
        private val readMap = HashMap<UUID, BluetoothGattCharacteristic>()
        private val notifyMap = HashMap<UUID, BluetoothGattCharacteristic>()
        private var serviceUUID: UUID = UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG)
        fun getReadCharacteristic(): HashMap<UUID, BluetoothGattCharacteristic> {
            return readMap
        }

        fun getWriteCharacteristic(): HashMap<UUID, BluetoothGattCharacteristic> {
            return writeMap
        }

        fun getNotifyGattCharacteristic(): HashMap<UUID, BluetoothGattCharacteristic> {
            return notifyMap
        }

        fun initCharacteristic() {
            bluetoothGatt?.services?.forEach {
                serviceUUID = it.uuid
                val characteristics = it.characteristics
                for (characteristic in characteristics) {
                    if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        readMap[serviceUUID] = characteristic
                    }
                    if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        writeMap[serviceUUID] = characteristic
                    }
                    if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        notifyMap[serviceUUID] = characteristic
                    }
                }
            }
        }

        fun clear() {
            readMap.clear()
            writeMap.clear()
            notifyMap.clear()
        }

    }

    private var bleMtuChangedCallback: IBleGattMtuChangedCallback? = null
    private val bleNotifyCallbackList = ArrayList<IBleGattNotifyCallback>()
    private val bleRssiCallbackList = ArrayList<IBleGattRssiCallback>()
    private val bleReadCallbackList = ArrayList<IBleReadCallback>()
    private var bluetoothGatt: BluetoothGatt? = null
    private var handler = Looper.myLooper()?.let { GattHandler(it) }
    private var connectCallback: IConnectCallback? = null
    private var connectTimeout: Long? = null//超时时长
    private var isStopConnect = true
    private var bluetoothGattChannel: BluetoothGattChannel = BluetoothGattChannel()
    private var messageDeal = MessageDeal()
    private var mtuCount = BleConstant.DEFAULT_MTU_COUNT
    private val bleConnectStateListenerList = ArrayList<IBleConnectStateListener>()
    private var isConnecting = false //是否正在连接
    private var isConnected = false
    private var isPreConnected: Boolean? = null
    private var pairFilter: IntentFilter? = null //配对Filter

    private val mPairReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                when (device!!.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        connectCallback?.onPairSuccess()
                        initConnect()
                    }

                    BluetoothDevice.BOND_NONE -> {
                        connectCallback?.onPairFailure(
                            BleException(
                                BleExceptionCode.PAIR_FAIL,
                                "Bluetooth pairing failed"
                            )
                        )
                        clearBluetoothState()
                    }

                    BluetoothDevice.BOND_BONDING -> {
                        removeConnectAndPair()
                        connectCallback?.onPairing()
                    }
                }
            }
        }
    }

    //初始化配对Filter
    private fun initPairFilter() {
        pairFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        BleConfig.instance.getContext().registerReceiver(mPairReceiver, pairFilter)
    }

    /**
     * 设备连接
     */
    @Synchronized
    override fun connect(
        bleDevice: BluetoothLeDevice,
        connectCallback: IConnectCallback,
        connectTimeout: Long?
    ) {
        handler?.removeCallbacksAndMessages(null)
        this.connectCallback = connectCallback
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout
        } else {
            this.connectTimeout = BleConfig.instance.getConnectTimeout()
        }
        initConnect()
    }

    /**
     * 设备连接
     */
    @SuppressLint("MissingPermission")
    private fun connect() {
        if (isConnected || isConnecting) {
            return
        }
        if(pairFilter == null){
            initPairFilter()
        }
        isConnecting = true
        bluetoothGatt = bluetoothBleDevice.device.connectGatt(
            BleConfig.instance.getContext(),
            BleConfig.instance.getGattAutoConnect(),
            bluetoothGattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    /**
     * 取消连接
     */

    @Synchronized
    override fun disconnect() {
        clearBluetoothState()
        sendConnectState()
    }

    private fun initConnect(){
        isStopConnect = false
        handler?.removeMessages(BleConstant.MSG_CONNECT_TIMEOUT)
        handler?.sendEmptyMessageDelayed(BleConstant.MSG_CONNECT_TIMEOUT, this.connectTimeout!!)
        connect()
    }

    private fun removeConnectAndPair(){
        handler?.removeCallbacksAndMessages(null)
        gattClose()
    }

    @SuppressLint("MissingPermission")
    private fun gattClose(){
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }
    private fun clearBluetoothState() {
        pairFilter?.let {
            BleConfig.instance.getContext().unregisterReceiver(mPairReceiver)
        }
        gattClose()
        GattConnectManager.instance.getMultiGattOpera().removeGattConnectMirror(this)
        bluetoothGatt=null
        messageDeal.clear()
        isConnected = false
        isConnecting = false
        isStopConnect = true
        clear(bluetoothBleDevice)
    }
    @Synchronized
    override fun clear(bleDevice: BluetoothLeDevice) {
        handler?.removeCallbacksAndMessages(null)
        bluetoothGattChannel.clear()
        connectCallback = null
        bleNotifyCallbackList.clear()
        bleRssiCallbackList.clear()
        bleReadCallbackList.clear()
        bleMtuChangedCallback = null
    }

    /**
     * 是否连接
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun isConnected(): Boolean {
        return isConnected
    }

    /**
     * 获取当前蓝牙唯一key
     */
    fun getDeviceUniqueKey(): String {
        return bluetoothBleDevice.getDeviceUniqueKey()
    }

    /**
     * 获取当前蓝牙设备
     */
    fun getBleDevice(): BluetoothLeDevice {
        return bluetoothBleDevice
    }

    /**
     * 获取BluetoothGatt
     */
    fun getBluetoothGatt(): BluetoothGatt? {
        return bluetoothGatt
    }

    @SuppressLint("MissingPermission")
    override fun addRssiListener(bleDevice: BluetoothLeDevice, bleRssiCallback: IBleGattRssiCallback) {
        if (bluetoothGatt != null) {
            if (!bluetoothGatt!!.readRemoteRssi()) {
                bleRssiCallback.onRssiFailure(
                    BleException(
                        BleExceptionCode.GATT_ERROR,
                        "gatt readRemoteRssi fail"
                    )
                )
            } else {
                bleRssiCallbackList.add(bleRssiCallback)
            }
        } else {
            bleRssiCallback.onRssiFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "BluetoothGatt is null"
                )
            )
        }
    }
    override fun removeRssiListener(bleDevice: BluetoothLeDevice, bleRssiCallback: IBleGattRssiCallback) {
        bleRssiCallbackList.remove(bleRssiCallback)
    }

    /**
     * 消息通知
     */
    override fun addNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?,
        uuidNotify: String?,
        userCharacteristicDescriptor: Boolean?
    ) {
        var mCharacteristic: BluetoothGattCharacteristic? = null
        if (uuidNotify == null) {
            bluetoothGattChannel.getNotifyGattCharacteristic().values.forEach {
                mCharacteristic = it
                return@forEach
            }
        } else {
            val mGattService = bluetoothGatt?.getService(UUID.fromString(uuidService))
            mCharacteristic = mGattService?.getCharacteristic(UUID.fromString(uuidNotify))
        }
        if (mCharacteristic != null) {
            setCharacteristicNotification(
                mCharacteristic!!,
                true,
                bleNotifyCallback,
                userCharacteristicDescriptor
            )
        } else {
            bleNotifyCallback.onNotifyFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "this characteristic not support read!"
                )
            )
        }
    }

    override fun removeNotifyMessageListener(
        bleDevice: BluetoothLeDevice,
        bleNotifyCallback: IBleGattNotifyCallback,
        uuidService: String?,
        uuidNotify: String?,
        userCharacteristicDescriptor: Boolean?
    ) {
        var mCharacteristic: BluetoothGattCharacteristic? = null
        if (uuidNotify == null) {
            bluetoothGattChannel.getNotifyGattCharacteristic().values.forEach {
                mCharacteristic = it
                return@forEach
            }
        } else {
            val mGattService = bluetoothGatt?.getService(UUID.fromString(uuidService))
            mCharacteristic = mGattService?.getCharacteristic(UUID.fromString(uuidNotify))
        }
        if (mCharacteristic != null) {
            setCharacteristicNotification(
                mCharacteristic!!,
                false,
                null,
                userCharacteristicDescriptor
            )
        }

        bleNotifyCallbackList.remove(bleNotifyCallback)
    }

    @SuppressLint("MissingPermission")
    private fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean,
        bleNotifyCallback: IBleGattNotifyCallback? = null,
        useCharacteristicDescriptor: Boolean? = true
    ) {
        val success1 = bluetoothGatt!!.setCharacteristicNotification(characteristic, enable)
        if (!success1) {
            bleNotifyCallback?.onNotifyFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "gatt setCharacteristicNotification fail"
                )
            )
            return
        }
        val descriptor: BluetoothGattDescriptor = if (useCharacteristicDescriptor!!) {
            characteristic.getDescriptor(characteristic.uuid)
        } else {
            characteristic.getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG))
        }
        descriptor.value =
            if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        val success2 = bluetoothGatt!!.writeDescriptor(descriptor)
        if (!success2) {
            bleNotifyCallback?.onNotifyFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "gatt writeDescriptor fail"
                )
            )
        } else {
            if (enable && bleNotifyCallback != null) {
                bleNotifyCallbackList.add(bleNotifyCallback)
            }
        }
    }

    override fun writeMsg(
        bleDevice: BluetoothLeDevice,
        data: ByteArray?,
        bleWriteCallback: IBleGattWriteCallback?,
        uuidService: String?,
        uuidWrite: String?
    ) {
        if (data == null || data.isEmpty()) {
            bleWriteCallback?.onWriteFailure(
                BleException(
                    BleExceptionCode.OTHER_FAIL,
                    "the data to be written is empty"
                )
            )
            return
        }
        var writeCharacteristic: BluetoothGattCharacteristic? = null
        if (uuidWrite == null) {
            bluetoothGattChannel.getWriteCharacteristic().values.forEach {
                writeCharacteristic = it
                return@forEach
            }
        } else {
            val mGattService = bluetoothGatt?.getService(UUID.fromString(uuidService))
            writeCharacteristic = mGattService?.getCharacteristic(UUID.fromString(uuidWrite))
        }
        if (writeCharacteristic != null) {
            SplitWriter(
                bluetoothGatt!!,
                writeCharacteristic!!,
                bleWriteCallback,
                data,
                mtuCount
            ).send()
        } else {
            bleWriteCallback?.onWriteFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "this characteristic not support write!"
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun addReadMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback,
        uuidService: String?,
        uuidRead: String?
    ) {
        var mCharacteristic: BluetoothGattCharacteristic? = null
        if (uuidRead == null) {
            bluetoothGattChannel.getReadCharacteristic().values.forEach {
                mCharacteristic = it
                return@forEach
            }
        } else {
            val mGattService = bluetoothGatt?.getService(UUID.fromString(uuidService))
            mCharacteristic = mGattService?.getCharacteristic(UUID.fromString(uuidRead))
        }

        if (mCharacteristic != null) {
            if (!bluetoothGatt!!.readCharacteristic(mCharacteristic)) {
                bleReadCallback.onReadFailure(
                    BleException(
                        BleExceptionCode.GATT_ERROR,
                        "gatt readCharacteristic fail"
                    )
                )
            } else {
                bleReadCallbackList.add(bleReadCallback)
            }
        } else {
            bleReadCallback.onReadFailure(
                BleException(
                    BleExceptionCode.GATT_ERROR,
                    "this characteristic not support read!"
                )
            )
        }
    }

    /**
     * set mtu
     */
    @SuppressLint("MissingPermission")
    override fun setMtu(
        bleDevice: BluetoothLeDevice,
        requiredMtu: Int,
        bleMtuChangedCallback: IBleGattMtuChangedCallback
    ) {
        if (!bluetoothGatt?.requestMtu(requiredMtu)!!) {
            bleMtuChangedCallback.onSetMTUFailure(
                BleException(BleExceptionCode.GATT_ERROR, "gatt requestMtu fail")
            )
        }else{
            this.bleMtuChangedCallback=bleMtuChangedCallback
        }
    }

    override fun getMtu(bleDevice: BluetoothLeDevice): Int {
        return mtuCount
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice,
        heartContent: String,
        interval: Long,
        uuidService: String?,
        uuidWrite: String?
    ) {
        startHeartbeat(bleDevice, heartContent.toByteArray(), interval, uuidService, uuidWrite)
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice, bytes: ByteArray, interval: Long, uuidService: String?,
        uuidWrite: String?
    ) {
        writeMsg(bleDevice, bytes, null, uuidService, uuidWrite)
        startHeartLoop(bleDevice,bytes,interval,uuidService,uuidWrite)
    }

    private fun startHeartLoop(bleDevice: BluetoothLeDevice, bytes: ByteArray, interval: Long,uuidService:String?, uuidWrite:String?){
        handler?.postDelayed({
            writeMsg(bleDevice, bytes, null,uuidService, uuidWrite)
            startHeartLoop(bleDevice,bytes,interval,uuidService, uuidWrite)
        }, interval)
    }

    override fun addConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        bleConnectStateListenerList.add(bleConnectStateListener)
    }

    override fun removeConnectStateListener(
        bleDevice: BluetoothLeDevice,
        bleConnectStateListener: IBleConnectStateListener
    ) {
        bleConnectStateListenerList.remove(bleConnectStateListener)
    }


    @Synchronized
    override fun removeReadMessageListener(
        bleDevice: BluetoothLeDevice,
        bleIBleReadCallback: IBleReadCallback
    ) {
        bleReadCallbackList.remove(bleIBleReadCallback)
    }
}
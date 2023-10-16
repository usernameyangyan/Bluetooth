package com.example.bluetooth.manager.connect.rfcomm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.common.IDeviceConnectMirror
import com.example.bluetooth.manager.common.MessageDeal
import com.example.bluetooth.manager.connect.impl.IBleConnectStateListener
import com.example.bluetooth.manager.connect.impl.IBleReadCallback
import com.example.bluetooth.manager.connect.impl.IConnectCallback
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.model.BluetoothLeDevice
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
Create by yangyan
Create time:2023/9/2 12:44
Describe:rfcomm 设备镜像
 */
class RfcommConnectMirror(private val bluetoothBleDevice: BluetoothLeDevice) : IDeviceConnectMirror,
    ICommonRfcommImpl {

    private var handler = Looper.myLooper()?.let { Handler(it) }
    private var isConnecting = false //是否正在连接
    private var isStopConnect = false //是否已经停止断连
    private var mmSocket: BluetoothSocket? = null
    private var pairFilter: IntentFilter? = null //配对Filter
    private var isCreateBonding = false //是否正在配对中
    private var bluetoothDeviceState = BluetoothDevice.BOND_NONE //蓝牙当前状态
    private var connectTimeout: Long? = null//超时时长
    private var connectCallback: IConnectCallback? = null
    private var mmInStream: InputStream? = null //读操作
    private var mmOutStream: OutputStream? = null //写操作
    private val bleReadCallbackList = ArrayList<IBleReadCallback>()
    private val bleConnectStateListenerList = ArrayList<IBleConnectStateListener>()
    private var messageDeal = MessageDeal()
    private var isConnected = false
    private var isPreConnected:Boolean?=null
    private var isLoopConnectedThread=true
    private val mPairReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action && isCreateBonding) {
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                val bondState = device!!.bondState
                if (bluetoothDeviceState != bondState) {
                    bluetoothDeviceState = bondState
                    when (bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            isCreateBonding = false
                            connectCallback?.onPairSuccess()
                            connect()
                        }

                        BluetoothDevice.BOND_NONE -> {
                            connectCallback?.onPairFailure(
                                BleException(
                                    BleExceptionCode.PAIR_FAIL,
                                    "Bluetooth pairing failed"
                                )
                            )
                        }

                        BluetoothDevice.BOND_BONDING -> {
                            connectCallback?.onPairing()
                        }
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
     * 连接流程
     */
    @SuppressLint("MissingPermission")
    private fun connect() {
        if (isConnecting||isConnected) {
            return
        }
        isConnecting = true
        isStopConnect = false
        connectTimeout?.let {
            handler?.postDelayed({
                isStopConnect = true
                mmSocket?.close()
            }, it)
        }
        while (mmSocket?.isConnected == false && !isStopConnect) {
            try {
                mmSocket?.connect()
            }catch (_:Exception){
                try {
                    val m: Method = bluetoothBleDevice.device.javaClass.getMethod(
                        "createRfcommSocket",
                        Int::class.javaPrimitiveType
                    )
                    mmSocket = m.invoke( bluetoothBleDevice.device, 1) as BluetoothSocket
                    mmSocket!!.connect()
                } catch (_: NoSuchMethodException) {
                } catch (_: InvocationTargetException) {
                } catch (_: IllegalAccessException) {
                } catch (_: IOException) {
                }
            }
        }
        if (isStopConnect) {
            connectCallback?.onConnectFailure(
                BleException(
                    BleExceptionCode.TIMEOUT,
                    "Connection timed out"
                )
            )
        }
        if (mmSocket?.isConnected == true) {
            isConnected = true
            mmInStream = mmSocket?.inputStream
            mmOutStream = mmSocket?.outputStream
            RfcommConnectManager.instance.getMultiRfcommOpera().addRfcommConnectMirror(this)
            connectCallback?.onConnectSuccess(bluetoothBleDevice)
            sendConnectState()
            read()
        }
        isConnecting = false
    }
    private fun read() {
        Thread{
            while (isConnected) {
                try {
                    val availableLen = mmInStream?.available()
                    availableLen?.let {
                        if (it > 0) {
                            val buffer = ByteArray(availableLen)
                            mmInStream?.read(buffer) // Read from the InputStream
                            val msg = if (BleConfig.instance.getMergeWholeMsgRule() != null) {
                                messageDeal.convertMessage(buffer)
                            } else {
                                buffer
                            }
                            for (bleReadCallback in bleReadCallbackList) {
                                msg?.let { msgValue ->
                                    bleReadCallback.onReadSuccess(msgValue)
                                }
                            }

                        }
                    }
                } catch (e: Exception) {
                    disconnect()
                    for (bleReadCallback in bleReadCallbackList) {
                        bleReadCallback.onReadFailure(
                            BleException(
                                BleExceptionCode.RFCOMM_ERROR,
                                "read failed:${e.message}"
                            )
                        )
                    }
                }

            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    override fun connect(
        bluetoothLeDevice: BluetoothLeDevice,
        connectCallback: IConnectCallback,
        connectTimeout: Long?
    ) {
        if(BleConfig.instance.getIsNeedPair()&&pairFilter == null){
            initPairFilter()
        }

        this.connectCallback = connectCallback
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout
        } else {
            this.connectTimeout = BleConfig.instance.getConnectTimeout()
        }

        if(BleConfig.instance.getIsNeedPair()){
            val bondState: Int = bluetoothBleDevice.device.bondState
            if (bondState == BluetoothDevice.BOND_BONDED) {
                mmSocket = bluetoothLeDevice.device.createRfcommSocketToServiceRecord(BleConfig.instance.getUUIDBySSP())
                connect()
            } else if (bondState == BluetoothDevice.BOND_NONE) {
                isCreateBonding = true
                bluetoothBleDevice.device.createBond();
            }
        }else{
            mmSocket =
                bluetoothLeDevice.device.createInsecureRfcommSocketToServiceRecord(BleConfig.instance.getUUIDBySSP())
            connect()
        }

    }

    override fun writeMsg(bleDevice: BluetoothLeDevice, msg: String): Boolean {
        return writeMsg(bleDevice, msg.toByteArray())
    }

    override fun writeMsg(bleDevice: BluetoothLeDevice, bytes: ByteArray): Boolean {
        if (!isConnected) {
            return false
        }
        try {
            mmOutStream?.write(bytes)
            mmOutStream?.flush()
        } catch (e: Exception) {
            disconnect()
            return false
        }
        return true
    }

    override fun startHeartbeat(
        bleDevice: BluetoothLeDevice,
        heartContent: String,
        interval: Long
    ) {
        startHeartbeat(bleDevice, heartContent.toByteArray(), interval)

    }

    override fun startHeartbeat(bleDevice: BluetoothLeDevice, bytes: ByteArray, interval: Long) {
        writeMsg(bleDevice, bytes)
        startHeartLoop(bleDevice,bytes,interval)
    }
    private fun startHeartLoop(bleDevice: BluetoothLeDevice, bytes: ByteArray, interval: Long){
        handler?.postDelayed({
            writeMsg(bleDevice, bytes)
            startHeartLoop(bleDevice,bytes,interval)
        }, interval)
    }

    override fun addMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback
    ) {
        bleReadCallbackList.add(bleReadCallback)
    }

    override fun removeMessageListener(
        bleDevice: BluetoothLeDevice,
        bleReadCallback: IBleReadCallback
    ) {
        bleReadCallbackList.remove(bleReadCallback)
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

    override fun disconnect() {
        isLoopConnectedThread=false
        isConnecting = false
        isConnected = false
        pairFilter?.let {
            BleConfig.instance.getContext().unregisterReceiver(mPairReceiver)
        }
        sendConnectState()
        connectCallback = null
        handler?.removeCallbacksAndMessages(null)
        mmInStream?.close()
        mmOutStream?.close()
        mmSocket?.close()
        messageDeal.clear()
        bleReadCallbackList.clear()
        bleConnectStateListenerList.clear()
        RfcommConnectManager.instance.getMultiRfcommOpera().removeRfcommConnectMirror(this)
    }

    private fun sendConnectState() {
        if(isPreConnected!=isConnected){
            for (bleConnectStateListener in bleConnectStateListenerList) {
                bleConnectStateListener.onConnectState(isConnected)
            }
            isPreConnected=isConnected
        }
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
     * 是否连接
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun isConnected(): Boolean {
        return isConnected
    }
}
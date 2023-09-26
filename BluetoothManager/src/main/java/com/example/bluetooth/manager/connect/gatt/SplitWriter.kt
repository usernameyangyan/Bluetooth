package com.example.bluetooth.manager.connect.gatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.example.bluetooth.manager.common.BleConstant
import com.example.bluetooth.manager.exception.BleException
import com.example.bluetooth.manager.exception.BleExceptionCode
import com.example.bluetooth.manager.connect.gatt.impl.IBleGattWriteCallback
import java.util.LinkedList
import java.util.Queue
import kotlin.math.roundToInt
/**
Create by yangyan
Create time:2023/9/2 10:00
Describe:发送消息根据mtu分割发送
 */
class SplitWriter(
    private val bluetoothGatt:BluetoothGatt,
    private val writeCharacteristic: BluetoothGattCharacteristic,
    private var bleWriteCallback: IBleGattWriteCallback?,
    private val data:ByteArray,
    private val mtuCount: Int
) {

    private var mHandlerThread: HandlerThread = HandlerThread("splitWriter")
    private var mHandler:Handler
    private val mDataQueue=splitByte(data)
    private var isSendSuccess:Boolean?=null
    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == BleConstant.MSG_SPLIT_WRITE_NEXT) {
                    write()
                }
            }
        }
    }

    fun send(){
        write()
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun write() {
        if (mDataQueue.peek() == null) {
            release()
            isSendSuccess?.let {
                if(it){
                    bleWriteCallback?.onWriteSuccess(data)
                }else{
                    bleWriteCallback?.onWriteFailure(BleException(BleExceptionCode.GATT_ERROR,"gatt write data failed"))
                }
            }
            return
        }
        val value=mDataQueue.poll()
        isSendSuccess = if (writeCharacteristic.setValue(value)) {
            bluetoothGatt.writeCharacteristic(writeCharacteristic)
        } else {
            false
        }
        val message = mHandler.obtainMessage(BleConstant.MSG_SPLIT_WRITE_NEXT)
        mHandler.sendMessageDelayed(message, 100)
    }

    /**
     * 数据写出分割
     */
    private fun splitByte(data: ByteArray): Queue<ByteArray> {
        val count = mtuCount - 3 //减去mtu 3个额外开销
        val byteQueue: Queue<ByteArray> = LinkedList()
        val pkgCount = if (data.size % count == 0) {
            data.size / count
        } else {
            (data.size / count + 1).toFloat().roundToInt()
        }
        if (pkgCount > 0) {
            for (i in 0 until pkgCount) {
                var dataPkg: ByteArray
                var j: Int
                if (pkgCount == 1 || i == pkgCount - 1) {
                    j = if (data.size % count == 0) count else data.size % count
                    System.arraycopy(data, i * count, ByteArray(j).also {
                        dataPkg = it
                    }, 0, j)
                } else {
                    System.arraycopy(data, i * count, ByteArray(count).also {
                        dataPkg = it
                    }, 0, count)
                }
                byteQueue.offer(dataPkg)
            }
        }
        return byteQueue
    }

    private fun release() {
        mHandlerThread.quit()
        mHandler.removeCallbacksAndMessages(null)
    }
}
package com.example.bluetooth.manager.common

import com.example.bluetooth.manager.utils.HexUtils
import com.example.bluetooth.manager.utils.OwnByteMergerUtils

/**
Create by yangyan
Create time:2023/8/31 15:17
Describe:蓝牙合包
 */
class MessageDeal {
    private var waiting = false
    private var totalLength = 0
    private var currentContentLength = 0
    private var dataLengthBytes = ByteArray(4)
    private var tempBuffer = ByteArray(0)
    private var receiveBuffer = ByteArray(0)
    private var topicLength = 0
    private var dataLength = 0

    private var headFlag=BleConfig.instance.getMergeWholeMsgRule()?.getHeadFlag()
    private var headByteSize=BleConfig.instance.getMergeWholeMsgRule()?.getHeadByteSize()
    private var messageByteSize=BleConfig.instance.getMergeWholeMsgRule()?.getMergeMessageByteSize()

    fun convertMessage(msgBuffer: ByteArray?):ByteArray? {
        if(msgBuffer==null){
           return null
        }
        var returnMsg:ByteArray?=null
        var currentMsgBuffer = msgBuffer
        if (tempBuffer.isNotEmpty()) {
            currentMsgBuffer = OwnByteMergerUtils.byteMerger(tempBuffer, currentMsgBuffer)
            tempBuffer = ByteArray(0)
        }
        val flagStr = StringBuilder()
        if (currentMsgBuffer.size >= headByteSize!! +messageByteSize!!) {
            for (i in 0 until headByteSize!!) {
                flagStr.append(String.format("%02x", currentMsgBuffer[i]))
            }
            if (flagStr.toString() == headFlag) {
                waiting = true
                currentContentLength = 0
                val lengthBuffer = currentMsgBuffer.copyOfRange(headByteSize!!, headByteSize!!+messageByteSize!!)
                totalLength = HexUtils.littleBytesToInt(lengthBuffer)
                returnMsg=dealMsg(currentMsgBuffer.copyOfRange(headByteSize!!+messageByteSize!!, currentMsgBuffer.size))
            } else if (waiting) {
                returnMsg=dealMsg(currentMsgBuffer)
            }
        } else {
            if (!waiting) {
                tempBuffer = OwnByteMergerUtils.byteMerger(tempBuffer, currentMsgBuffer)
            } else {
                returnMsg=dealMsg(currentMsgBuffer)
            }
        }
        return returnMsg
    }

    private fun dealMsg(buffer: ByteArray):ByteArray? {
        if (currentContentLength + buffer.size >= totalLength) {
            val remindCount = totalLength - currentContentLength
            receiveBuffer = OwnByteMergerUtils.byteMerger(
                receiveBuffer,
                buffer.copyOfRange(0, remindCount)
            )
            if (buffer.size - remindCount > 0) {
                convertMessage(buffer.copyOfRange(remindCount, buffer.size))
            }
            waiting = false
            currentContentLength = 0
            val msg=receiveBuffer.copyOfRange(0,receiveBuffer.size)
            receiveBuffer= ByteArray(0)
            return msg
        } else {
            currentContentLength += buffer.size
            receiveBuffer = OwnByteMergerUtils.byteMerger(receiveBuffer, buffer)
            return null
        }
    }


    fun clear(){
        waiting = false
        totalLength = 0
        currentContentLength = 0
        dataLengthBytes =  ByteArray(0)
        tempBuffer = ByteArray(0)
        receiveBuffer = ByteArray(0)
        topicLength = 0
        dataLength = 0
    }
}
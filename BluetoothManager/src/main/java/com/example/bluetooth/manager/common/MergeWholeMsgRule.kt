package com.example.bluetooth.manager.common
/**
Create by yangyan
Create time:2023/8/31 14:54
Describe:接收蓝牙消息组拼规则
 */
class MergeWholeMsgRule private constructor(){
    private lateinit var headFlag:String
    private var headByteSize:Int=0
    private var messageByteSize:Int=0

    companion object {
        val rule: MergeWholeMsgRule by lazy { MergeWholeMsgRule() }
    }

    /**
     * @param headFlag 开始头标志
     * @param headByteSize 头标志字节长度
     */
    fun setMergeHeadFlag(headFlag:String,headByteSize:Int):MergeWholeMsgRule{
        this.headFlag=headFlag
        this.headByteSize=headByteSize
        return this
    }
    /**
     *  获取头标志
     */
    fun getHeadFlag():String{
        return headFlag
    }
    /**
     *  获取头标志字节长度
     */
    fun getHeadByteSize():Int{
        return headByteSize
    }
    /**
     * @param messageByteSize 内容字节长度
     */
    fun setMergeMessageByteSize(messageByteSize:Int):MergeWholeMsgRule{
        this.messageByteSize=messageByteSize
        return this
    }
    /**
     *  获取内容字节长度
     */
    fun getMergeMessageByteSize():Int{
        return messageByteSize
    }
}
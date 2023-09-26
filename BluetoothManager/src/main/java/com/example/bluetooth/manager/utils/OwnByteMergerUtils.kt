package com.example.bluetooth.manager.utils

/**
Create by yangyan
Create time:2023/8/31 15:14
Describe:字节数组操作
 */
object OwnByteMergerUtils {
    fun byteMerger(vararg byteList: ByteArray): ByteArray {
        var lengthByte = 0
        for (bytes in byteList) {
            lengthByte += bytes.size
        }
        val allByte = ByteArray(lengthByte)
        var countLength = 0
        for (bytes in byteList) {
            System.arraycopy(bytes, 0, allByte, countLength, bytes.size)
            countLength += bytes.size
        }
        return allByte
    }
    fun subByte(b: ByteArray?, off: Int, length: Int): ByteArray {
        val b1 = ByteArray(length)
        System.arraycopy(b, off, b1, 0, length)
        return b1
    }
}
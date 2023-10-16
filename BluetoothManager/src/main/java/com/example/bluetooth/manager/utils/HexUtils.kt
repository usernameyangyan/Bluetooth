package com.example.bluetooth.manager.utils

/**
Create by yangyan
Create time:2023/8/31 15:28
Describe:
 */
object HexUtils {
    //按高位排序
    fun bigBytesToInt(bytes: ByteArray): Int {
        var addr: Int
        when (bytes.size) {
            1 -> addr = bytes[0].toInt() and 0xFF
            2 -> {
                addr = bytes[0].toInt() and 0xFF
                addr = addr shl 8 or (bytes[1].toInt() and 0xFF)
            }

            else -> {
                addr = bytes[0].toInt() and 0xFF
                addr = addr shl 8 or (bytes[1].toInt() and 0xFF)
                addr = addr shl 8 or (bytes[2].toInt() and 0xFF)
                addr = addr shl 8 or (bytes[3].toInt() and 0xFF)
            }
        }
        return addr
    }
    fun littleBytesToInt(bytes: ByteArray): Int {
        var addr: Int
        when (bytes.size) {
            1 -> addr = bytes[0].toInt() and 0xFF
            2 -> {
                addr = bytes[0].toInt() and 0xFF
                addr = addr or (bytes[1].toInt() shl 8 and 0xFF00)
            }

            else -> {
                addr = bytes[0].toInt() and 0xFF
                addr = addr or (bytes[1].toInt() shl 8 and 0xFF00)
                addr = addr or (bytes[2].toInt() shl 16 and 0xFF0000)
                addr = addr or (bytes[3].toInt() shl 24 and -0x1000000)
            }
        }
        return addr
    }
}
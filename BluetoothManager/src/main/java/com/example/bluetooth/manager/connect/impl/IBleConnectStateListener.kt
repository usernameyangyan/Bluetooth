package com.example.bluetooth.manager.connect.impl

/**
Create by yangyan
Create time:2023/9/2 19:08
Describe:蓝牙连接状态
 */
interface IBleConnectStateListener {
    fun onConnectState(isConnect:Boolean)
}
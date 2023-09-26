package com.example.bluetoothdemo

import android.app.Application
import com.example.bluetooth.manager.common.BleConfig
import com.example.bluetooth.manager.common.MergeWholeMsgRule
import com.example.bluetooth.manager.scan.BleScanType

/**
Create by yangyan
Create time:2023/9/2 19:26
Describe:
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BleConfig.instance.setMergeWholeMsgRule(
            MergeWholeMsgRule.rule.setMergeHeadFlag("a55a", 2).setMergeMessageByteSize(4)
        ).setBleScanType(BleScanType.SCAN_DISCOVERY).init(this)
    }
}
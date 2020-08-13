package com.fh.payday.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadBroadcastReceiver(
    private val dm: DownloadManager,
    private val refId: Long,
    private val block: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
            val query = DownloadManager.Query()
            query.setFilterById(refId)
            val c = dm.query(query)
            if (c.moveToFirst()) {
                val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    block()
                }
            }
        }
    }

}
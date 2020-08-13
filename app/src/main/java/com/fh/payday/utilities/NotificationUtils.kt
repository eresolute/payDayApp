package com.fh.payday.utilities

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.fh.payday.R

fun createNotificationChannel(context: Context) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_channel)
        val descriptionText = context.getString(R.string.channel_desc)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(context.getString(R.string.channel_id), name, importance).apply { description = descriptionText }

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}

/**
 * Method checks if the app is in background or not
 */
fun isAppInBackground(context: Context): Boolean {
    var isInBackground = true
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
    val runningProcesses = am.runningAppProcesses
    for (processInfo in runningProcesses) {
        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            for (activeProcess in processInfo.pkgList) {
                if (activeProcess == context.packageName) {
                    isInBackground = false
                }
            }
        }
    }
    /*} else {
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equals(context.getPackageName())) {
            isInBackground = false;
        }
    }*/
    return isInBackground
}
package com.code.damahe.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.checkSelfPermission
import com.code.damahe.res.R

object Permissions {
    fun checkRunningConditions(context: Context, getAll: List<Permission>): Boolean {
        for (permission in getAll) {
            if (checkSelfPermission(context, permission.id) != PERMISSION_GRANTED && permission.isRequired) {
                return false
            }
        }

        return true
    }

    /**
     * for Manifest.permission.POST_NOTIFICATIONS,
     * add <uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> permission in AndroidManifest.xml
     *
     * for Manifest.permission.WRITE_EXTERNAL_STORAGE,
     * add <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29"/> permission in AndroidManifest.xml
     *
     **/
    fun getAllPermission(): List<Permission> {
        val permissions: MutableList<Permission> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(
                Permission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    R.string.msg_notification_permission,
                    R.string.msg_notification_permission_summary
                )
            )
        }

        if (Build.VERSION.SDK_INT in 24..32) {
            permissions.add(
                Permission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    R.string.msg_storage_permission,
                    R.string.msg_storage_permission_summary
                )
            )
        } else {
            permissions.add(
                Permission(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    R.string.msg_storage_permission,
                    R.string.msg_storage_permission_summary
                )
            )
        }

        return permissions
    }

}

data class Permission(
    val id: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val isRequired: Boolean = true
)
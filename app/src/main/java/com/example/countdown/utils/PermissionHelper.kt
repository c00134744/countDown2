package com.example.countdown.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限处理工具类
 * 处理应用所需的各种权限
 */
object PermissionHelper {
    
    // 权限请求码
    const val REQUEST_CODE_VIBRATE = 1001
    const val REQUEST_CODE_NOTIFICATION = 1002
    const val REQUEST_CODE_WAKE_LOCK = 1003
    const val REQUEST_CODE_ALL_PERMISSIONS = 1000
    
    // 所需权限列表
    private val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
        add(Manifest.permission.VIBRATE)
        add(Manifest.permission.WAKE_LOCK)
        
        // Android 13+ 需要通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    /**
     * 检查是否有震动权限
     */
    fun hasVibratePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13以下不需要运行时权限
        }
    }
    
    /**
     * 检查是否有屏幕常亮权限
     */
    fun hasWakeLockPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WAKE_LOCK
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有所有必需权限
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取缺失的权限列表
     */
    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 请求震动权限
     */
    fun requestVibratePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.VIBRATE),
            REQUEST_CODE_VIBRATE
        )
    }
    
    /**
     * 请求通知权限
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATION
            )
        }
    }
    
    /**
     * 请求屏幕常亮权限
     */
    fun requestWakeLockPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WAKE_LOCK),
            REQUEST_CODE_WAKE_LOCK
        )
    }
    
    /**
     * 请求所有必需权限
     */
    fun requestAllRequiredPermissions(activity: Activity) {
        val missingPermissions = getMissingPermissions(activity)
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                REQUEST_CODE_ALL_PERMISSIONS
            )
        }
    }
    
    /**
     * 处理权限请求结果
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        when (requestCode) {
            REQUEST_CODE_ALL_PERMISSIONS,
            REQUEST_CODE_VIBRATE,
            REQUEST_CODE_NOTIFICATION,
            REQUEST_CODE_WAKE_LOCK -> {
                val deniedPermissions = mutableListOf<String>()
                
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i])
                    }
                }
                
                if (deniedPermissions.isEmpty()) {
                    onAllGranted()
                } else {
                    onDenied(deniedPermissions)
                }
            }
        }
    }
    
    /**
     * 检查是否应该显示权限说明
     */
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 获取权限说明文本
     */
    fun getPermissionRationaleText(permission: String): String {
        return when (permission) {
            Manifest.permission.VIBRATE -> "需要震动权限来在计时结束时提醒您"
            Manifest.permission.POST_NOTIFICATIONS -> "需要通知权限来显示计时进度和完成提醒"
            Manifest.permission.WAKE_LOCK -> "需要屏幕常亮权限来在计时期间保持屏幕亮起"
            else -> "此权限对应用正常运行是必需的"
        }
    }
}
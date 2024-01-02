package com.sobot.sobotchatsdkdemo.permission

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import com.sobot.sobotchatsdkdemo.permission.ZCPermissionUtils.findDeniedPermissions
import com.sobot.sobotchatsdkdemo.permission.ZCPermissionUtils.getActivity
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by Yang on 2017/9/20.
 * desc: 一个专门用于动态权限的工具类
 */
class ZCPermission private constructor(private val `object`: Any) {
    private lateinit var mPermissions: Array<String>
    private var mRequestCode = 0
    fun permissions(vararg permissions: String): ZCPermission {
        mPermissions = permissions as Array<String>
        return this
    }

    fun addRequestCode(requestCode: Int): ZCPermission {
        mRequestCode = requestCode
        return this
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    fun request() {
        zcPermissionCallback = null
        requestPermissions(`object`, mRequestCode, mPermissions)
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    fun request(callback: ZCPermissionCallback?) {
        if (callback != null) {
            zcPermissionCallback = callback
        }
        requestPermissions(`object`, mRequestCode, mPermissions)
    }

    interface ZCPermissionCallback {
        //请求权限成功
        fun permissionSuccess(requsetCode: Int)

        //请求权限失败
        fun permissionFail(requestCode: Int)
    }

    companion object {
        private var zcPermissionCallback: ZCPermissionCallback? = null
        fun with(activity: Activity): ZCPermission {
            return ZCPermission(activity)
        }

        fun with(fragment: Fragment): ZCPermission {
            return ZCPermission(fragment)
        }

        fun needPermission(activity: Activity, requestCode: Int, permissions: Array<String>) {
            zcPermissionCallback = null
            requestPermissions(activity, requestCode, permissions)
        }

        fun needPermission(fragment: Fragment, requestCode: Int, permissions: Array<String>) {
            zcPermissionCallback = null
            requestPermissions(fragment, requestCode, permissions)
        }

        fun needPermission(
            activity: Activity,
            requestCode: Int,
            permissions: Array<String>,
            callback: ZCPermissionCallback?
        ) {
            if (callback != null) {
                zcPermissionCallback = callback
            }
            requestPermissions(activity, requestCode, permissions)
        }

        fun needPermission(
            fragment: Fragment,
            requestCode: Int,
            permissions: Array<String>,
            callback: ZCPermissionCallback?
        ) {
            if (callback != null) {
                zcPermissionCallback = callback
            }
            requestPermissions(fragment, requestCode, permissions)
        }

        fun needPermission(activity: Activity?, requestCode: Int, permission: String) {
            zcPermissionCallback = null
            if (activity != null) {
                needPermission(activity, requestCode, arrayOf(permission))
            }
        }

        fun needPermission(fragment: Fragment?, requestCode: Int, permission: String) {
            zcPermissionCallback = null
            if (fragment != null) {
                needPermission(fragment, requestCode, arrayOf(permission))
            }
        }

        fun needPermission(
            activity: Activity?,
            requestCode: Int,
            permission: String,
            callback: ZCPermissionCallback?
        ) {
            if (callback != null) {
                zcPermissionCallback = callback
            }
            if (activity != null) {
                needPermission(activity, requestCode, arrayOf(permission))
            }
        }

        fun needPermission(
            fragment: Fragment?,
            requestCode: Int,
            permission: String,
            callback: ZCPermissionCallback?
        ) {
            if (callback != null) {
                zcPermissionCallback = callback
            }
            if (fragment != null) {
                needPermission(fragment, requestCode, arrayOf(permission))
            }
        }

        /**
         * 请求权限
         *
         * @param object
         * @param requestCode
         * @param permissions
         */
        @TargetApi(value = Build.VERSION_CODES.M)
        private fun requestPermissions(
            `object`: Any,
            requestCode: Int,
            permissions: Array<String>
        ) {
            if (!ZCPermissionUtils.isOverMarshmallow) {
                if (zcPermissionCallback != null) {
                    zcPermissionCallback!!.permissionSuccess(requestCode)
                } else {
                    doExecuteSuccess(`object`, requestCode)
                }
                return
            }
            val deniedPermissions: List<String> =
                findDeniedPermissions(getActivity(`object`), permissions)
            /**
             * 先检查是否有没有授予的权限，有的话请求，没有的话就直接执行权限授予成功的接口/注解方法
             */
            if (deniedPermissions.size > 0) {
                if (`object` is Activity) {
                    (`object` as Activity).requestPermissions(
                        deniedPermissions.toTypedArray<String>(),
                        requestCode
                    )
                } else if (`object` is Fragment) {
                    `object`.requestPermissions(deniedPermissions.toTypedArray(), requestCode)
                } else {
                    throw IllegalArgumentException(`object`.javaClass.name + " is not supported")
                }
            } else {
                if (zcPermissionCallback != null) {
                    zcPermissionCallback!!.permissionSuccess(requestCode)
                } else {
                    doExecuteSuccess(`object`, requestCode)
                }
            }
        }

        private fun doExecuteSuccess(activity: Any, requestCode: Int) {
            val executeMethod = ZCPermissionUtils.findMethodWithRequestCode(
                activity.javaClass,
                ZCPermissionSuccess::class.java, requestCode
            )
            Companion.executeMethod(activity, executeMethod)
        }

        private fun doExecuteFail(activity: Any, requestCode: Int) {
            val executeMethod = ZCPermissionUtils.findMethodWithRequestCode(
                activity.javaClass,
                ZCPermissionFail::class.java, requestCode
            )
            Companion.executeMethod(activity, executeMethod)
        }

        private fun executeMethod(activity: Any, executeMethod: Method?) {
            if (executeMethod != null) {
                try {
                    if (!executeMethod.isAccessible) executeMethod.isAccessible = true
                    executeMethod.invoke(activity, *arrayOf())
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }

        fun onRequestPermissionsResult(
            activity: Activity, requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
        ) {
            requestResult(activity, requestCode, permissions, grantResults)
        }

        fun onRequestPermissionsResult(
            fragment: Fragment, requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
        ) {
            requestResult(fragment, requestCode, permissions, grantResults)
        }

        /**
         * 有回调接口的话(即回调接口不为空的话)先执行回调接口的方法，若为空，则寻找响应的注解方法。
         *
         * @param obj
         * @param requestCode
         * @param permissions
         * @param grantResults
         */
        private fun requestResult(
            obj: Any, requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
        ) {
            val deniedPermissions: MutableList<String> = ArrayList()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            if (deniedPermissions.size > 0) {
                if (zcPermissionCallback != null) {
                    zcPermissionCallback!!.permissionFail(requestCode)
                } else {
                    doExecuteFail(obj, requestCode)
                }
            } else {
                if (zcPermissionCallback != null) {
                    zcPermissionCallback!!.permissionSuccess(requestCode)
                } else {
                    doExecuteSuccess(obj, requestCode)
                }
            }
        }
    }
}
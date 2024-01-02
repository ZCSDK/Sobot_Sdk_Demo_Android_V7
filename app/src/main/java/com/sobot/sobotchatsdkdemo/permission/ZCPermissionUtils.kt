package com.sobot.sobotchatsdkdemo.permission

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Build
import androidx.core.content.PermissionChecker
import java.lang.reflect.Method

/**
 * 一个专门用于动态权限的工具类
 */
object ZCPermissionUtils {
    /**
     * 判断系统版本是否大于6.0
     *
     * @return
     */
    val isOverMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /**
     * 从申请的权限中找出未授予的权限
     *
     * @param activity
     * @param permission
     * @return
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    fun findDeniedPermissions(activity: Activity?, vararg permission: Array<String>): List<String> {
        val denyPermissions: MutableList<String> = ArrayList()
        for (value in permission) {
            if (!selfPermissionGranted(activity, value)) {
                denyPermissions.add(value.toString())
            }
        }
        return denyPermissions
    }

    fun selfPermissionGranted(context: Context?, permission: Array<String>): Boolean {
        return PermissionChecker.checkSelfPermission(
            context!!,
            permission!!.toString()
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    /**
     * 寻找相应的注解方法
     *
     * @param clazz  寻找的那个类
     * @param clazz1 响应的注解的标记
     * @return
     */
    fun findAnnotationMethods(clazz: Class<*>, clazz1: Class<out Annotation?>?): List<Method> {
        val methods: MutableList<Method> = ArrayList()
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(clazz1)) {
                methods.add(method)
            }
        }
        return methods
    }

    fun <A : Annotation?> findMethodPermissionFailWithRequestCode(
        clazz: Class<*>,
        permissionFailClass: Class<A>?, requestCode: Int
    ): Method? {
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(permissionFailClass)) {
                if (requestCode == method.getAnnotation(ZCPermissionFail::class.java)
                        .requestCode
                ) {
                    return method
                }
            }
        }
        return null
    }

    /**
     * 找到那个相应的注解方法且requestCode与需要的一样
     *
     * @param m
     * @param clazz
     * @param requestCode
     * @return
     */
    fun isEqualRequestCodeFromAnntation(m: Method, clazz: Class<*>, requestCode: Int): Boolean {
        return if (clazz == ZCPermissionFail::class.java) {
            requestCode == m.getAnnotation(ZCPermissionFail::class.java).requestCode
        } else if (clazz == ZCPermissionSuccess::class.java) {
            requestCode == m.getAnnotation(ZCPermissionSuccess::class.java)
                .requestCode
        } else {
            false
        }
    }

    fun <A : Annotation?> findMethodWithRequestCode(
        clazz: Class<*>,
        annotation: Class<A>, requestCode: Int
    ): Method? {
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(annotation)) {
                if (isEqualRequestCodeFromAnntation(method, annotation, requestCode)) {
                    return method
                }
            }
        }
        return null
    }

    fun <A : Annotation?> findMethodPermissionSuccessWithRequestCode(
        clazz: Class<*>,
        permissionFailClass: Class<A>?, requestCode: Int
    ): Method? {
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(permissionFailClass)) {
                if (requestCode == method.getAnnotation(ZCPermissionSuccess::class.java)
                        .requestCode
                ) {
                    return method
                }
            }
        }
        return null
    }

    fun getActivity(`object`: Any?): Activity? {
        if (`object` is Fragment) {
            return `object`.activity
        } else if (`object` is Activity) {
            return `object`
        }
        return null
    }
}
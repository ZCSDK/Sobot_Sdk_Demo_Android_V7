package com.sobot.sobotchatsdkdemo.permission

import android.Manifest
import android.os.Build

/**
 * 由于Android8.0的限制 最好的做法是申请权限的时候一组一组的申请
 */
object Permission {
    val CALENDAR: Array<String>
    val CAMERA: Array<String>
    val CONTACTS: Array<String>
    val LOCATION: Array<String>
    val MICROPHONE: Array<String>
    val PHONE: Array<String>
    val SENSORS: Array<String>
    val SMS: Array<String>
    val STORAGE: Array<String>

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            CALENDAR = arrayOf()
            CAMERA = arrayOf()
            CONTACTS = arrayOf()
            LOCATION = arrayOf()
            MICROPHONE = arrayOf()
            PHONE = arrayOf()
            SENSORS = arrayOf()
            SMS = arrayOf()
            STORAGE = arrayOf()
        } else {
            CALENDAR = arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
            CAMERA = arrayOf(
                Manifest.permission.CAMERA
            )
            CONTACTS = arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.GET_ACCOUNTS
            )
            LOCATION = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            MICROPHONE = arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
            PHONE = arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.USE_SIP,
                Manifest.permission.PROCESS_OUTGOING_CALLS
            )
            SENSORS = arrayOf(
                Manifest.permission.BODY_SENSORS
            )
            SMS = arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_WAP_PUSH,
                Manifest.permission.RECEIVE_MMS
            )
            STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        }
    }
}
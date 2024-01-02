package com.sobot.sobotchatsdkdemo.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.*

/**
 * 数据保存
 */
object SobotSPUtil {
    private var sharedPreferences: SharedPreferences? = null
    private const val CONFIG = "sobot_demo_config"
    fun saveStringData(context: Context, key: String?, value: String?) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putString(key, value).commit()
    }

    fun saveBooleanData(context: Context, key: String?, value: Boolean) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putBoolean(key, value).commit()
    }

    fun getBooleanData(context: Context, key: String?, defValue: Boolean): Boolean {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getBoolean(key, defValue)
    }

    fun getStringData(context: Context, key: String?, defValue: String?): String? {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getString(key, defValue)
    }

    fun saveIntData(context: Context, key: String?, value: Int) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putInt(key, value).commit()
    }

    fun saveLongData(context: Context, key: String?, value: Long) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putLong(key, value).commit()
    }

    fun getIntData(context: Context, key: String?, defValue: Int): Int {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getInt(key, defValue)
    }

    fun getLongData(context: Context, key: String?, defValue: Long): Long {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getLong(key, defValue)
    }

    fun removeKey(context: Context, key: String?) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().remove(key).commit()
    }

    /**
     * 使用SharedPreference保存对象
     *
     * @param key        储存对象的key
     * @param saveObject 储存的对象
     */
    fun saveObject(context: Context, key: String?, saveObject: Any) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        val editor = sharedPreferences!!.edit()
        val string = Object2String(saveObject)
        editor.putString(key, string)
        editor.commit()
    }

    /**
     * 获取SharedPreference保存的对象
     *
     * @param key 储存对象的key
     * @return object 返回根据key得到的对象
     */
    fun getObject(context: Context, key: String?): Any? {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        }
        val string = sharedPreferences!!.getString(key, null)
        return string?.let { String2Object(it) }
    }

    /**
     * writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
     * 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
     *
     * @param object 待加密的转换为String的对象
     * @return String   加密后的String
     */
    private fun Object2String(`object`: Any): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null
        return try {
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(`object`)
            val string = String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
            objectOutputStream.close()
            string
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 使用Base64解密String，返回Object对象
     *
     * @param objectString 待解密的String
     * @return object      解密后的object
     */
    private fun String2Object(objectString: String): Any? {
        val mobileBytes = Base64.decode(objectString.toByteArray(), Base64.DEFAULT)
        val byteArrayInputStream = ByteArrayInputStream(mobileBytes)
        var objectInputStream: ObjectInputStream? = null
        return try {
            objectInputStream = ObjectInputStream(byteArrayInputStream)
            val `object` = objectInputStream.readObject()
            objectInputStream.close()
            `object`
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
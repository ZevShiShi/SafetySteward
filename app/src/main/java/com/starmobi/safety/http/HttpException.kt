package com.starmobi.safety.http

import android.accounts.NetworkErrorException
import android.util.MalformedJsonException
import com.blankj.utilcode.util.LogUtils
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object HttpException {

    /**
     * 处理异常，提示错误信息
     */
    fun catchException(e: Throwable) {
        e.printStackTrace()
        when (e) {
            is HttpException -> {
                catchHttpException(e.code())
            }

            is SocketTimeoutException -> {
                showToast(e.message!!)
            }

            is UnknownHostException, is NetworkErrorException -> {
                showToast(e.message!!)
            }

            is MalformedJsonException, is JsonSyntaxException -> {
                showToast(e.message!!)
            }

            is InterruptedIOException -> {
                showToast("服务器连接失败，请稍后重试")
            }

            is ConnectException -> {
                showToast("连接服务器失败")
            }

            else -> {
                showToast("其他异常==$e")
            }
        }
    }

    /**
     * 处理网络异常
     */
    private fun catchHttpException(errorCode: Int) {
        if (errorCode in 200 until 300) return// 成功code则不处理
        showToast("", errorCode)
    }


    /**
     * toast提示
     */
    private fun showToast(errorMsg: String, errorCode: Int = -1) {
        LogUtils.e("errorMsg==$errorMsg,errorCode===$errorCode")
    }

}
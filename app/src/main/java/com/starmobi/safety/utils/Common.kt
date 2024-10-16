package com.starmobi.safety.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.webkit.WebSettings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.starmobi.safety.application.getAppContext
import com.starmobi.safety.detector.AudioDetector
import com.starmobi.safety.http.HttpException
import com.starmobi.safety.socket.AppClientSocket
import com.starmobi.safety.socket.AppServerSocket
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


fun getAppServerSocket() = AppServerSocket.INSTANCE
fun getAppClientSocket() = AppClientSocket.INSTANCE
fun getAudioDetector() = AudioDetector.INSTANCE


/***
 * 防止快速点击
 */
fun View.onClick(listener: (view: View) -> Unit) {
    val minTime = 500L
    var lastTime = 0L
    this.setOnClickListener {
        val tmpTime = System.currentTimeMillis()
        if (tmpTime - lastTime > minTime) {
            lastTime = tmpTime
            listener.invoke(this)
        } else {
            LogUtils.dTag("Onclick", "点击过快，取消触发")
        }
    }
}

fun Context.startActivityEx(targetClass: Class<*>) {
    val intent = Intent(this, targetClass)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun Context.goToPrivacyPolicy() {
    // 隐私协议
    val uri = Uri.parse("https://www.lovelink.store/today/static/privacy/v2/index.html")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun Context.goToWeb(link: String) {
    // 隐私协议
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun Context.getColorEx(colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

fun Context.getDrawableEx(drawableResId: Int): Drawable {
    return ContextCompat.getDrawable(this, drawableResId)!!
}


/**
 * ViewModel扩展方法：启动协程
 * @param block 协程逻辑
 * @param onError 错误回调方法
 * @param onComplete 完成回调方法
 */
fun ViewModel.launch(
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = { _: Throwable -> },
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        run {
            // 这里统一处理错误
            HttpException.catchException(throwable)
            onError(throwable)
        }
    }) {
        try {
            block.invoke(this)
        } finally {
            onComplete()
        }
    }
}


/**
 * ViewModel扩展方法：启动协程
 * @param block 协程逻辑
 * @param onError 错误回调方法
 * @param onComplete 完成回调方法
 */
fun reqLaunch(
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = { _: Throwable -> },
    onComplete: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, throwable ->
        run {
            // 这里统一处理错误
            HttpException.catchException(throwable)
            onError(throwable)
        }
    }) {
        try {
            withContext(Dispatchers.IO) {
                block.invoke(this)
            }
        } finally {
            onComplete()
        }
    }
}


fun getUserAgent(): String {
    val userAgent = StringBuilder()
    //获取本地user_agent
    var userAgentString = try {
        WebSettings.getDefaultUserAgent(getAppContext())
    } catch (e: Exception) {
        System.getProperty("http.agent") ?: ""
    }

    LogUtils.d("userAgentString=========$userAgentString")
    userAgent.append("$userAgentString/")
    userAgent.append("${AppUtils.getAppVersionName()}/")
    userAgent.append("${android.os.Build.BRAND}/")
    userAgent.append("${android.os.Build.MODEL}/")
    userAgent.append("${android.os.Build.VERSION.RELEASE}/")
    userAgent.append("${Locale.getDefault().language}/")
    return userAgent.toString()
}



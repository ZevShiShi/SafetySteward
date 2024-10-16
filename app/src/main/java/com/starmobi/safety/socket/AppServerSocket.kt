package com.starmobi.safety.socket

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ObjectUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket


/**
 * Socket服务端
 */
class AppServerSocket {


    companion object {
        // 单例模式
        val INSTANCE: AppServerSocket by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppServerSocket()
        }
    }


    @Volatile
    private var isConnected = false
    private var mServerSocket: ServerSocket? = null
    private var mSocket: Socket? = null
    private var mBufferedWriter: BufferedWriter? = null
    private var mBufferedReader: BufferedReader? = null
    private val mPort = 1989


    fun createServerSocket(finishListener: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                LogUtils.d("createServerSocket========${Thread.currentThread().name}")
                try {
                    mServerSocket = ServerSocket(mPort)
                    //等待客户端的连接，Accept会阻塞，直到建立连接，
                    //所以需要放在子线程中运行。
                    LogUtils.d("createServerSocket start0")
                    mSocket = mServerSocket?.accept()
                    LogUtils.d("createServerSocket start1")

                    //获取输入流
                    mBufferedReader = BufferedReader(InputStreamReader(mSocket?.getInputStream()))
                    //获取输出流
                    mBufferedWriter = BufferedWriter(OutputStreamWriter(mSocket?.getOutputStream()))
                    isConnected = true

                } catch (e: Exception) {
                    isConnected = false
                    e.printStackTrace()
                }
            }
            finishListener.invoke()
        }
    }

    fun isConnected(): Boolean = isConnected

    @OptIn(DelicateCoroutinesApi::class)
    fun readMsg(readListener: (msg: String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            var text = ""
            while (isConnected) {
                //循环执行read，用来接收数据。
                try {
                    LogUtils.d("readMsg start0")
                    var line = mBufferedReader?.readLine() ?: ""
                    if (ObjectUtils.isNotEmpty(line)) {
                        LogUtils.d("readMsg start1 line=$line")
                        text = line
                        CoroutineScope(Dispatchers.Main).launch {
                            readListener.invoke(text)
                        }
                    }
                } catch (e: Exception) {
                    isConnected = false
                    e.printStackTrace()
                }
            }
        }
    }

    fun writeMsg(str: String) {
        if (ObjectUtils.isEmpty(str)) {
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                //发送
                try {
                    mBufferedWriter?.write(str)
                    mBufferedWriter?.newLine()
                    mBufferedWriter?.flush()
                } catch (e: Exception) {
                    isConnected = false
                    e.printStackTrace()
                }
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun close() {
        GlobalScope.launch(Dispatchers.IO) {
            isConnected = false
            try {
                mBufferedReader?.close()
                mBufferedWriter?.close()

                LogUtils.d("AppServerSocket1 close=${!mSocket?.isClosed!!}")
                if (!mSocket?.isClosed!!) {
                    mSocket?.shutdownInput()
                    mSocket?.shutdownOutput()
                    mSocket?.getInputStream()?.close()
                    mSocket?.getOutputStream()?.close()
                    mSocket?.close()
                }
                LogUtils.d("AppServerSocket2 close=${!mSocket?.isClosed!!}")

                if (!mServerSocket?.isClosed!!) {
                    mServerSocket?.close()
                }

//                mServerSocket = null
//                mSocket = null
//                mBufferedWriter = null
//                mBufferedReader = null

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
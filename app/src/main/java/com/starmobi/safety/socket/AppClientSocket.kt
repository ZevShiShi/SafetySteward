package com.starmobi.safety.socket

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ObjectUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Socket服务端
 */
class AppClientSocket {


    companion object {
        // 单例模式
        val INSTANCE: AppClientSocket by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppClientSocket()
        }
    }


    @Volatile
    private var isConnected = false
    private var mSocket: Socket? = null
    private var mBufferedWriter: BufferedWriter? = null
    private var mBufferedReader: BufferedReader? = null
    private val mPort = 1989
    private val mIp = NetworkUtils.getIPAddress(true)


    fun createClientSocket(finishListener: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                LogUtils.d("createClientSocket====ip==$mIp,port=$mPort")
                try {
                    //指定ip地址和端口号
                    mSocket = Socket()
                    mSocket?.reuseAddress = true
                    val socketAddress = InetSocketAddress(mIp, mPort)
                    mSocket?.connect(socketAddress)

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
    private fun readMsg(readListener: (msg: String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            var text = ""
            while (isConnected) {
                //循环执行read，用来接收数据。
                //数据存在buffer中，count为读取到的数据长度。
                try {
                    LogUtils.d("readMsg start0")
                    val line = mBufferedReader?.readLine() ?: ""
                    if (ObjectUtils.isNotEmpty(line)) {
                        text = line
                        CoroutineScope(Dispatchers.Main).launch {
                            readListener.invoke(text)
                        }
                    }
                } catch (e: Exception) {
                    isConnected = false
                    e.printStackTrace()
                }
                LogUtils.d("readMsg str=$text")
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
                    // 如果使用BufferedWriter 不加newline()和flush()的话
                    // 服务端BufferedReader 的readline()方法就读取不到msg,会一直阻塞下去
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
                mBufferedWriter?.close()
                mBufferedReader?.close()
                LogUtils.d("AppClientSocket1 close=${!mSocket?.isClosed!!}")
                if (!mSocket?.isClosed!!) {
                    mSocket?.shutdownInput()
                    mSocket?.shutdownOutput()
                    mSocket?.getInputStream()?.close()
                    mSocket?.getOutputStream()?.close()
                    mSocket?.close()
                }
                LogUtils.d("AppClientSocket2 close=${!mSocket?.isClosed!!}")
                mSocket = null
                mBufferedReader = null
                mBufferedWriter = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
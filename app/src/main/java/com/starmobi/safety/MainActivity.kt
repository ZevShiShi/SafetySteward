package com.starmobi.safety

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.permissionx.guolindev.PermissionX
import com.starmobi.safety.utils.getAppClientSocket
import com.starmobi.safety.utils.getAppServerSocket
import com.starmobi.safety.utils.onClick
import android.Manifest;
import com.starmobi.safety.utils.getAudioDetector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        PermissionX.init(this).permissions(
            Manifest.permission.RECORD_AUDIO,
        ).request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                LogUtils.d("已授权=============")
                getAudioDetector().detectAudio()
            }
        }


        findViewById<Button>(R.id.btnConnect).onClick {
            getAppServerSocket().createServerSocket {
                LogUtils.d("createServerSocket=====${getAppServerSocket().isConnected()}")
            }
            getAppClientSocket().createClientSocket {
                LogUtils.d("createClientSocket=====${getAppClientSocket().isConnected()}")
            }
        }

        findViewById<Button>(R.id.btnDisconnect).onClick {
            getAppServerSocket().close()
            getAppClientSocket().close()
        }

        findViewById<Button>(R.id.btnReceiveMessage).onClick {
            getAppServerSocket().readMsg { msg ->
                LogUtils.d("readMsg=$msg")
                findViewById<TextView>(R.id.tvServerText).text = msg
            }
//            if (getAppServerSocket().isConnected()) {
//                getAppServerSocket().readMsg { msg ->
//                    LogUtils.d("readMsg=$msg")
//                    findViewById<TextView>(R.id.tvServerText).text = msg
//                }
//            } else {
//                LogUtils.d("服务端没连接")
//            }
        }

        findViewById<Button>(R.id.btnSend).onClick {
            val text = findViewById<EditText>(R.id.etText).text.toString().trim()
            getAppClientSocket().writeMsg(text)
//            if (getAppClientSocket().isConnected()) {
//                getAppClientSocket().writeMsg(text)
//            } else {
//                LogUtils.d("客户端没连接")
//            }
        }


    }
}
package com.starmobi.safety.application

import android.app.Activity
import android.app.Application
import android.content.Context

fun getAppContext() = SafeApplication.mContext

class SafeApplication : Application() {

    companion object {
        lateinit var mContext: Context
        val mActivityList = mutableListOf<Activity>()
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }

}
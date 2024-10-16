package com.starmobi.safety.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import com.starmobi.safety.application.SafeApplication.Companion.mActivityList
import java.lang.reflect.ParameterizedType


open abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity() {

    lateinit var viewModel: VM
    lateinit var binding: VB
    protected var enterTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityList.add(this)
        initViewBinding()
        createViewModel()
        initView(savedInstanceState)
        initData(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            val option =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.navigationBarColor = getColorEx(R.color.primary_bg_color)
            window.statusBarColor = Color.TRANSPARENT
        }
    }


    private fun initViewBinding() {
        binding = inflateBinding()
        setContentView(binding.root)
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData(savedInstanceState: Bundle?)
    abstract fun inflateBinding(): VB


    /**
     * 创建 ViewModel
     */
    private fun createViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val tp = type.actualTypeArguments[0]
            val tClass = tp as? Class<VM> ?: BaseViewModel::class.java
            viewModel = ViewModelProvider(this)[tClass] as VM
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)
    }


    private fun isAppExit(): Boolean {
        return mActivityList.size == 1
    }


    override fun onBackPressed() {
        LogUtils.d("onBackPressed mActivityList=$mActivityList")
        if (isAppExit()) {
            LogUtils.d("完全退出app")
//            TvApplication.uploadExitData(this)
        }
        super.onBackPressed()
    }

}
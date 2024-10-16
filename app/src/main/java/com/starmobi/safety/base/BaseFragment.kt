package com.starmobi.safety.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

open abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> : Fragment() {

    lateinit var viewModel: VM
    lateinit var binding: VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = inflateBinding(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createViewModel()
        initView(view, savedInstanceState)
        initData()
    }

    //    abstract fun getLayoutId(): Int
    abstract fun initView(view: View, savedInstanceState: Bundle?)
    abstract fun initData()
    abstract fun inflateBinding(inflater: LayoutInflater): VB


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

}
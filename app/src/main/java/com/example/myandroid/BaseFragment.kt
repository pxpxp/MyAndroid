package com.example.myandroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author pxp
 * @description
 */
abstract class BaseFragment<T : ViewBinding> : Fragment() {
    private var _binding: T? = null
    val binding by lazy { _binding!! }

    open fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?) {
        val c = javaClass
        var cs = c.superclass
        val type: ParameterizedType
        while (true) {
            if (c.genericSuperclass is ParameterizedType) {
                type = c.genericSuperclass as ParameterizedType
                break
            }
            if (cs.genericSuperclass is ParameterizedType) {
                type = cs.genericSuperclass as ParameterizedType
                break
            } else {
                cs = c.superclass
            }
        }

        try {
            val cls = type.actualTypeArguments[0] as Class<*>
            val method = cls.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            _binding = method.invoke(null, inflater, container, false) as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initData()
    }

    /**
     * 初始化页面数据
     */
    abstract fun initView()

    abstract fun initData()

    /**
     * 在UI线程执行某个操作
     */
    fun post(cb: () -> Unit) {
        binding.root.post {
            cb.invoke()
        }
    }

    /**
     * 在UI线程执行某个操作
     */
    fun postDelayed(cb: () -> Unit, time: Long) {
        binding.root.postDelayed({
            cb.invoke()
        }, time)
    }

    override fun onHiddenChanged(hidden: Boolean) {
    }
}
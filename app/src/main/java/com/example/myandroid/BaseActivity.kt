package com.example.myandroid

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author pxp
 * @description
 */
abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    val binding by lazy { initViewBinding()!! }

    open fun initViewBinding(): T? {
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

        return try {
            val cls = type.actualTypeArguments[0] as Class<*>
            val method = cls.getMethod("inflate", LayoutInflater::class.java)
            method.invoke(null, layoutInflater) as T
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
//        val type = javaClass.genericSuperclass
//        if (type is ParameterizedType) {
//            try {
//                val cls = type.actualTypeArguments[0] as Class<*>
//                val method = cls.getMethod("inflate", LayoutInflater::class.java)
//                return method.invoke(null, layoutInflater) as T
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initData()
    }

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
}
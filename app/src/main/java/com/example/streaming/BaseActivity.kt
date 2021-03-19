package com.example.streaming

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.Unbinder
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import xyz.tanwb.airship.BaseApplication
import xyz.tanwb.airship.utils.StatusBarUtils
import xyz.tanwb.airship.view.BasePresenter
import xyz.tanwb.airship.view.widget.SwipeBackLayout
import java.io.Serializable
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<T : BasePresenter<*>> : RxAppCompatActivity() {
    /**
     * 获取上下文对象
     */
    var context: Context? = null
        protected set

    /**
     * 获取Activity实例
     */
    var activity: AppCompatActivity? = null
        protected set
    protected var mApplication: BaseApplication? = null
    protected var mPresenter: T? = null
    protected var unbinder: Unbinder? = null
    protected var isOnClick = true
    protected var noLinitClicks: List<Int>? = null
    protected var clickSleepTime = 300L
    protected var oldClickTime: Long = 0
    protected var views: SparseArray<View?>? = null
    private var ivShadow: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(getLayoutId())
        if (hasLightMode()) {
            StatusBarUtils.setStatusBarMode(this, true)
        }
        unbinder = ButterKnife.bind(this)
        context = this
        activity = this
        mApplication = application as BaseApplication?
        mApplication!!.addActivity(this)
        mPresenter = getT(this, 0)
        initView(savedInstanceState)
        initPresenter()
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(LayoutInflater.from(this).inflate(layoutResID, null))
    }

    override fun setContentView(view: View) {
        if (hasWindowBackground()) {
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground))
        }
        if (hasSwipeFinish()) {
            val swipeBackLayout = SwipeBackLayout(this)
            swipeBackLayout.setOnSwipeBackListener(object : SwipeBackLayout.SwipeBackListener {
                override fun onViewPositionChanged(
                    fractionAnchor: Float,
                    fractionScreen: Float
                ) {
                    ivShadow!!.alpha = 1 - fractionScreen
                }

                override fun onFinish() {
                    exit()
                }
            })
            swipeBackLayout.addView(view)
            ivShadow = ImageView(this)
            ivShadow!!.setBackgroundColor(Color.parseColor("#7F000000"))
            val container = RelativeLayout(this)
            container.addView(
                ivShadow,
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
            )
            container.addView(swipeBackLayout)
            super.setContentView(container)
        } else {
            super.setContentView(view)
        }
    }

    abstract fun getLayoutId(): Int

    protected override fun onStart() {
        super.onStart()
        if (context == null) {
            context = this
        }
        if (activity == null) {
            activity = this
        }
        if (mApplication == null) {
            mApplication = getApplication() as BaseApplication?
        }
    }

    protected override fun onResume() {
        super.onResume()
    }

    protected override fun onPause() {
        super.onPause()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        unbinder!!.unbind()
        if (mPresenter != null) {
//            mPresenter.onDestroy()
        }
        mPresenter = null
        activity = null
        context = null
        mApplication!!.removeActivity(this)
        mApplication = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            exit()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initPresenter()

    /**
     * 是否使用高亮模式
     */
    fun hasLightMode(): Boolean {
        return false
    }

    /**
     * 是否设置窗口背景
     */
    fun hasWindowBackground(): Boolean {
        return true
    }

    /**
     * 是否使用滑动返回
     */
    fun hasSwipeFinish(): Boolean {
        return true
    }

    /**
     * 是否可以继续执行点击时间
     */
    fun isCanClick(view: View): Boolean {
        if (isOnClick) {
            val newClickView = view.id
            if (noLinitClicks != null && noLinitClicks!!.size > 0) {
                for (viewId in noLinitClicks!!) {
                    if (newClickView == viewId) {
                        return true
                    }
                }
            }
            val newClickTime = System.currentTimeMillis()
            oldClickTime = if (newClickTime - oldClickTime < clickSleepTime) {
                return false
            } else {
                newClickTime
            }
            return true
        }
        return false
    }

    /**
     * 显示进度动画
     */
    fun showProgress() {}

    /**
     * 隐藏进度动画
     */
    fun hideProgress() {}

    /**
     * 跳转Activity并接收返回数据
     */
    fun advance(cls: Class<*>, vararg params: Any) {
        advance(getAdvanceIntent(cls, *params))
    }

    /**
     * 跳转Activity并接收返回数据
     */
    fun advance(clsName: String, vararg params: Any) {
        advance(getAdvanceIntent(clsName, *params))
    }

    /**
     * 跳转Activity并接收返回数据
     */
    fun advance(intent: Intent?) {
        startActivity(intent)
        overridePendingTransition(R.anim.view_in_from_right, R.anim.view_out_to_left)
    }

    /**
     * 跳转Activity并接收返回数据
     */
    fun advanceForResult(
        cls: Class<*>,
        requestCode: Int,
        vararg params: Any
    ) {
        advanceForResult(getAdvanceIntent(cls, *params), requestCode)
    }

    /**
     * 跳转Activity并接收返回数据
     */
    fun advanceForResult(
        clsName: String,
        requestCode: Int,
        vararg params: Any
    ) {
        advanceForResult(getAdvanceIntent(clsName, *params), requestCode)
    }

    /**
     * 跳转Activity并接收返回数据
     */
    fun advanceForResult(intent: Intent?, requestCode: Int) {
        startActivityForResult(intent, requestCode)
        overridePendingTransition(R.anim.view_in_from_right, R.anim.view_out_to_left)
    }

    private fun getAdvanceIntent(cls: Class<*>, vararg params: Any): Intent? {
        val intent = Intent()
        intent.setClass(this, cls)
        return putParams(intent, *params)
    }

    private fun getAdvanceIntent(clsName: String, vararg params: Any): Intent? {
        val intent = Intent()
        intent.setClassName(this, clsName)
        return putParams(intent, *params)
    }

    private fun putParams(intent: Intent?, vararg params: Any): Intent? {
        if (intent != null && params != null && params.size > 0) {
            for (i in 0 until params.size) {
                intent.putExtra("p$i", params[i] as Serializable)
            }
        }
        return intent
    }
    /**
     * 退出当前页面
     */
    /**
     * 退出当前页面
     */
    @JvmOverloads
    fun exit(isAnim: Boolean = true) {
        finish()
        if (isAnim) {
            overridePendingTransition(R.anim.view_in_from_left, R.anim.view_out_to_right)
        }
    }

    /**
     * 获取引用实体
     */
    fun <T> getT(o: Any, i: Int): T? {
        try {
            return ((o.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[i] as Class<T>).newInstance()
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 获取控件对象
     */
    fun <V : View?> getView(viewId: Int): V? {
        if (views == null) {
            views = SparseArray()
        }
        var view = views!![viewId]
        if (view == null) {
            view = findViewById(viewId)
            views!!.put(viewId, view)
        }
        return view as V?
    }
}

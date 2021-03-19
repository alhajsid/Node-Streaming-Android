package com.example.streaming

import android.animation.Animator
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import cn.nodemedia.NodeCameraView
import com.example.streaming.contract.PushContract

class GoLiveActivity : BaseActivity<PushContract.Presenter>(), PushContract.View, View.OnClickListener {

    private var pushSurface: NodeCameraView? = null
    private var pushBack: ImageView? =
        null
    private  var pushSwitch: ImageView? = null
    private  var pushFlash: ImageView? = null
    private var pushButton: TextView? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_go_live
    }

    override fun getmContext(): Context {
        return this
    }



    override fun getmActivity(): AppCompatActivity {
        return this
    }

    override fun initView(savedInstanceState: Bundle?) {
//        StatusBarUtils.setColorToTransparent(this);
        val sp =
            PreferenceManager.getDefaultSharedPreferences(this)
        val videoOrientation = sp.getString("video_orientation", "0")!!.toInt()
        requestedOrientation = if (videoOrientation == 1) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else if (videoOrientation == 2) {
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        assignViews()
    }


    /**
     * 实例化视图控件
     */
    private fun assignViews() {
        pushSurface = getView(R.id.push_surface)
        pushBack = getView(R.id.push_back)
        pushBack!!.setOnClickListener(this)
        pushSwitch = getView<ImageView>(R.id.push_switch)
        pushSwitch!!.setOnClickListener(this)
        pushFlash = getView<ImageView>(R.id.push_flash)
        pushFlash!!.setOnClickListener(this)
        pushButton = getView(R.id.push_button)
        pushButton!!.setOnClickListener(this)
    }

    override fun initPresenter() {
        mPresenter!!.initPresenter(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.push_back -> exit()
            R.id.push_switch -> {
                mPresenter!!.switchCamera()
                FlipAnimatorXViewShow(pushSwitch!!, pushSwitch!!, 300)
            }
            R.id.push_flash -> mPresenter!!.switchFlash()
            R.id.push_button -> {
                mPresenter!!.pushChange()
                pushButton!!.text = "push_wait"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun FlipAnimatorXViewShow(
        oldView: View,
        newView: View,
        time: Long
    ) {
        val animator1 =
            ObjectAnimator.ofFloat(oldView, "rotationY", 0f, 90f)
        val animator2 =
            ObjectAnimator.ofFloat(newView, "rotationY", -90f, 0f)
        animator2.interpolator = OvershootInterpolator(2.0f)
        animator1.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                oldView.visibility = View.GONE
                animator2.setDuration(time).start()
                newView.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator1.setDuration(time).start()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun exit() {
        finish()
    }

    override fun getNodeCameraView(): NodeCameraView? {
        return pushSurface
    }

    override fun buttonAvailable(isStarting: Boolean) {
//        pushButton.setClickable(true);
        pushButton!!.setBackgroundColor(if (isStarting) 0x3FF9493B else 0x3F000000)
        pushButton!!.setText(if (isStarting) "push_stop" else "push_start")
    }

    override fun buttonUnavailability() {
//        pushButton.setClickable(false);
//        pushButton.setTextColor(mContext.getResources().getColor(R.color.colorGray));
    }

    override fun flashChange(onOrOff: Boolean) {
        if (onOrOff) {
            pushFlash!!.setImageResource(R.drawable.ic_flash_on)
        } else {
            pushFlash!!.setImageResource(R.drawable.ic_flash_off)
        }
    }
}
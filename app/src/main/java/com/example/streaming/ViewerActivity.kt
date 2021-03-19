package com.example.streaming

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import cn.nodemedia.NodePlayerView
import com.example.streaming.contract.PlayContract
import xyz.tanwb.airship.utils.StatusBarUtils
import xyz.tanwb.airship.view.BaseActivity


class ViewerActivity : BaseActivity<PlayContract.Presenter?>(),
    PlayContract.View, View.OnClickListener {
    var mnodePlayerView: NodePlayerView? = null /////////////////////bug here
        private set
    private var playBack: ImageView? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_viewer
    }

    override fun getNodePlayerView(): NodePlayerView {
        return mnodePlayerView!!
    }

    override fun initView(savedInstanceState: Bundle) {
        StatusBarUtils.setColorToTransparent(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        assignViews()
    }

    /**
     * 实例化视图控件
     */
    private fun assignViews() {
        mnodePlayerView = getView(R.id.play_surface)
        playBack = getView(R.id.play_back)
        playBack!!.setOnClickListener(this)
    }

    override fun initPresenter() {
        mPresenter!!.initPresenter(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.play_back -> exit()
        }
    }

    public override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    public override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun exit() {
        finish()
    }

}

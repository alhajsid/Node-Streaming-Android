package cn.nodemedia;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;

import static cn.nodemedia.NodePlayerView.RenderType.SURFACEVIEW;
import static cn.nodemedia.NodePlayerView.RenderType.TEXTUREVIEW;
import static cn.nodemedia.NodePlayerView.UIViewContentMode.ScaleToFill;


/**
 * Created by ALiang on 16/12/28.
 */

public class NodePlayerView extends FrameLayout implements SurfaceHolder.Callback, TextureView.SurfaceTextureListener {
    public enum RenderType {
        SURFACEVIEW,
        TEXTUREVIEW
    }

    public enum UIViewContentMode {
        ScaleToFill,
        ScaleAspectFit,
        ScaleAspectFill
    }

    interface RenderCallback {
        void onSurfaceCreated(@NonNull Surface surface);

        void onSurfaceChanged(int width, int height);

        void onSurfaceDestroyed();
    }

    private Context mContext;
    private RenderCallback mRenderCallback;
    private View renderView;
    private RenderType mCurrentRenderType;
    private UIViewContentMode mUIViewContentMode = ScaleToFill;

    private FrameLayout.LayoutParams deflp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER);

    private Surface mSurface;
    private int mCanvasWidth = 0;
    private int mCanvasHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private float mZoomScale = 1.0f;
    private boolean isSurfaceCreate = false;
    private boolean isMediaOverlay = false;

    public NodePlayerView(Context context) {
        super(context);
        initView(context);
    }

    public NodePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NodePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NodePlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }


    private void initView(Context context) {
        mContext = context;
        setRenderType(SURFACEVIEW);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCanvasWidth = getWidth();
        mCanvasHeight = getHeight();
        if(isSurfaceCreate && mVideoWidth > 0 && mVideoWidth > 0) {
            setVideoSize(mVideoWidth,mVideoHeight);
        }

    }

    public void setRenderCallback(RenderCallback callback) {
        mRenderCallback = callback;
        if (isSurfaceCreate) {
            //错过了创建回调触发时机,手动回调
            mRenderCallback.onSurfaceCreated(mSurface);
        }
    }

    public void setRenderType(RenderType renderType) {
        if (mCurrentRenderType == renderType)
            return;
        if (mCurrentRenderType == SURFACEVIEW) {
            SurfaceView sv = (SurfaceView) renderView;
            sv.getHolder().removeCallback(this);
            removeView(renderView);
            renderView = null;
        } else if (mCurrentRenderType == TEXTUREVIEW) {
            TextureView tv = (TextureView) renderView;
            tv.setSurfaceTextureListener(null);
            removeView(renderView);
            renderView = null;
        }

        if (renderType == SURFACEVIEW) {
            SurfaceView sv = new SurfaceView(mContext);
            sv.getHolder().addCallback(this);
            sv.setLayoutParams(deflp);
            renderView = sv;
            addView(renderView);
        } else if (renderType == TEXTUREVIEW) {
            TextureView tv = new TextureView(mContext);
            tv.setSurfaceTextureListener(this);
            tv.setLayoutParams(deflp);
            renderView = tv;
            addView(renderView);
        }
        mCurrentRenderType = renderType;
    }

    public RenderType getRenderType() {
        return mCurrentRenderType;
    }

    public View getRenderView() {
        return renderView;
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        renderView.setVisibility(visibility);
    }

    public void setVideoSize(final int width, final int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        setUIViewContentMode(mUIViewContentMode);
    }

    public void setZoomScale(float zoomScale) {
        this.mZoomScale = zoomScale;
    }

    public void setUIViewContentMode(UIViewContentMode mode) {
        mUIViewContentMode = mode;
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                float specAspectRatio = (float) mCanvasWidth / (float) mCanvasHeight;
                float displayAspectRatio = (float) mVideoWidth / (float) mVideoHeight;
                boolean shouldBeWider = displayAspectRatio > specAspectRatio;
                int fixWidth = mCanvasWidth;
                int fixHeight = mCanvasHeight;
                switch (mUIViewContentMode) {
                    case ScaleToFill:
                        fixWidth = mCanvasWidth;
                        fixHeight = mCanvasHeight;
                        break;
                    case ScaleAspectFit:
                        if (shouldBeWider) {
                            // too wide, fix width
                            fixWidth = mCanvasWidth;
                            fixHeight = (int) (fixWidth / displayAspectRatio);
                        } else {
                            // too high, fix height
                            fixHeight = mCanvasHeight;
                            fixWidth = (int) (fixHeight * displayAspectRatio);
                        }
                        break;
                    case ScaleAspectFill:
                        if (shouldBeWider) {
                            // not high enough, fix height
                            fixHeight = mCanvasHeight;
                            fixWidth = (int) (fixHeight * displayAspectRatio);
                        } else {
                            // not wide enough, fix width
                            fixWidth = mCanvasWidth;
                            fixHeight = (int) (fixWidth / displayAspectRatio);
                        }

                        break;
                }
                fixWidth *= mZoomScale;
                fixHeight *= mZoomScale;
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        fixWidth,
                        fixHeight,
                        Gravity.CENTER);
                renderView.setLayoutParams(lp);
                requestLayout();
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isSurfaceCreate = true;
        mSurface = surfaceHolder.getSurface();
        if (mRenderCallback != null) {
            mRenderCallback.onSurfaceCreated(mSurface);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mRenderCallback != null) {
            mRenderCallback.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isSurfaceCreate = false;
        mSurface = null;
        if (mRenderCallback != null) {
            mRenderCallback.onSurfaceDestroyed();
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        isSurfaceCreate = true;
        mSurface = new Surface(surfaceTexture);
        if (mRenderCallback != null) {
            mRenderCallback.onSurfaceCreated(mSurface);
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int wdith, int height) {
        if (mRenderCallback != null) {
            mRenderCallback.onSurfaceChanged(wdith, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        isSurfaceCreate = false;
        if (mRenderCallback != null) {
            mSurface = null;
            surfaceTexture.release();
            mRenderCallback.onSurfaceDestroyed();
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

}

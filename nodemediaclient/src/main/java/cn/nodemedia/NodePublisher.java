package cn.nodemedia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${$USER_NAME} on 17/4/11.
 */

public class NodePublisher implements NodeCameraView.NodeCameraViewCallback {
    static {
        System.loadLibrary("NodeMediaClient");
    }

    private long id;
    private WindowManager wm = null;
    private NodePublisherDelegate mNodePublisherDelegate;
    private NodePublisherVideoTextureDelegate mNodePublisherVideoTextureDelegate;
    private CapturePictureListener mCapturePictureListener;
    private NodeCameraView mNodeCameraView;
    private static AudioManager.OnAudioFocusChangeListener sAudioFocusChangeListener = null;
    private static List<NodePublisher> publishers = new ArrayList<>(0);

    private String outputUrl;
    private String pageUrl;
    private String swfUrl;
    private String connArgs;

    private boolean isFrontCamera;
    private boolean isDisplayFrontMirror;
    private boolean isStartPreview;

    private int cameraId;
    private int cameraOri;
    private int cameraWidth;
    private int cameraHeight;
    private int surfaceOri;
    private int surfaceWidth;
    private int surfaceHeight;
    private int logLevel;

    public static final int VIDEO_PPRESET_16X9_270 = 0;
    public static final int VIDEO_PPRESET_16X9_360 = 1;
    public static final int VIDEO_PPRESET_16X9_480 = 2;
    public static final int VIDEO_PPRESET_16X9_540 = 3;
    public static final int VIDEO_PPRESET_16X9_720 = 4;
    public static final int VIDEO_PPRESET_16X9_1080 = 5;

    public static final int VIDEO_PPRESET_4X3_270 = 10;
    public static final int VIDEO_PPRESET_4X3_360 = 11;
    public static final int VIDEO_PPRESET_4X3_480 = 12;
    public static final int VIDEO_PPRESET_4X3_540 = 13;
    public static final int VIDEO_PPRESET_4X3_720 = 14;
    public static final int VIDEO_PPRESET_4X3_1080 = 15;

    public static final int VIDEO_PPRESET_1X1_270 = 20;
    public static final int VIDEO_PPRESET_1X1_360 = 21;
    public static final int VIDEO_PPRESET_1X1_480 = 22;
    public static final int VIDEO_PPRESET_1X1_540 = 23;
    public static final int VIDEO_PPRESET_1X1_720 = 24;
    public static final int VIDEO_PPRESET_1X1_1080 = 25;

    public static final int AUDIO_PROFILE_LCAAC = 0;
    public static final int AUDIO_PROFILE_HEAAC = 1;
    public static final int AUDIO_PROFILE_SPEEX = 2;

    public static final int VIDEO_PROFILE_BASELINE = 0;
    public static final int VIDEO_PROFILE_MAIN = 1;
    public static final int VIDEO_PROFILE_HIGH = 2;
    public static final int VIDEO_PROFILE_HEVC_MAIN = 3;

    public static final int CAMERA_BACK = 0;
    public static final int CAMERA_FRONT = 1;

    public static final int NM_PIXEL_BGRA = 1;
    public static final int NM_PIXEL_RGBA = 2;

    public static final int NM_LOGLEVEL_ERROR = 0;
    public static final int NM_LOGLEVEL_INFO = 1;
    public static final int NM_LOGLEVEL_DEBUG = 2;

    public NodePublisher(@NonNull Context context) {
        this(context, "");
    }

    public NodePublisher(@NonNull Context context, @NonNull String license) {
        this.id = jniInit(context, license);
        this.outputUrl = "";
        this.pageUrl = "";
        this.swfUrl = "";
        this.connArgs = "";
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (sAudioFocusChangeListener == null) {
            sAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        for (NodePublisher publisher : publishers) {
                            //麦克风静音
                            publisher.jniAudioMuted(true);
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        for (NodePublisher publisher : publishers) {
                            //麦克风恢复
                            publisher.jniAudioMuted(false);
                        }
                    }
                }
            };
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.requestAudioFocus(sAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        }
        publishers.add(this);
    }

    public void release() {
        final NodePublisher self = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                self.mNodePublisherDelegate = null;
                self.mNodeCameraView = null;
                self.wm = null;
                self.jniDeInit();
                self.jniFreeGPUImage();
                self.id = 0;
                publishers.remove(self);
            }
        }).start();

    }

    private int getWindowRotation() {
        return wm.getDefaultDisplay().getRotation();
    }

    public void setOutputUrl(@NonNull String outputUrl) {
        this.outputUrl = outputUrl.trim();
    }

    public void setPageUrl(@NonNull String pageUrl) {
        this.pageUrl = pageUrl.trim();
    }

    public void setSwfUrl(@NonNull String swfUrl) {
        this.swfUrl = swfUrl.trim();
    }

    public void setConnArgs(@NonNull String connArgs) {
        this.connArgs = connArgs;
    }

    public void setCameraPreview(@NonNull NodeCameraView cameraPreview, int cameraID, boolean frontMirror) {
        mNodeCameraView = cameraPreview;
        mNodeCameraView.setNodeCameraViewCallback(this);
        cameraId = cameraID;
        isDisplayFrontMirror = frontMirror;
    }

    public void setAudioParam(int bitrate, int profile) {
        jniSetAudioParam(bitrate, profile, 44100);
    }

    public void setAudioParam(int bitrate, int profile, int sampleRate) {
        jniSetAudioParam(bitrate, profile, sampleRate);
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int switchCamera() {
        if (mNodeCameraView == null) {
            return -1;
        }
        int ret = mNodeCameraView.switchCamera();
        cameraId = ret >= 0 ? ret : cameraId;
        return ret;
    }

    public int startPreview() {
        if (mNodeCameraView == null) {
            return -1;
        }
        int ret = mNodeCameraView.startPreview(cameraId);
        isFrontCamera = mNodeCameraView.isFrontCamera();
        cameraOri = mNodeCameraView.getCameraOrientation();
        surfaceOri = getWindowRotation();
        if (ret == 0) {
            isStartPreview = true;
        }
        return ret;

    }

    public int stopPreview() {
        if (mNodeCameraView == null) {
            return -1;
        }
        isStartPreview = false;
        return mNodeCameraView.stopPreview();
    }

    public int setZoomScale(int zoomScale) {
        if (mNodeCameraView == null) {
            return -1;
        }
        if (zoomScale < 0 || zoomScale > 100) {
            return -2;
        }
        return jniSetScaleGPUImage(zoomScale);
    }

    public int setFlashEnable(boolean flashEnable) {
        if (mNodeCameraView == null) {
            return -1;
        }
        return mNodeCameraView.setFlashEnable(flashEnable);
    }

    public int setAutoFocus(boolean autoFocus) {
        if (mNodeCameraView == null) {
            return -1;
        }
        return mNodeCameraView.setAutoFocus(autoFocus);
    }

    public void capturePicture(CapturePictureListener listener) {
        this.mCapturePictureListener = listener;
        jniRequestScreenShot();
    }

    public void setNodePublisherDelegate(@NonNull NodePublisherDelegate delegate) {
        this.mNodePublisherDelegate = delegate;
    }

    public void setNodePublisherVideoTextureDelegate(@NonNull NodePublisherVideoTextureDelegate nodePublisherVideoTextureDelegate) {
        this.mNodePublisherVideoTextureDelegate = nodePublisherVideoTextureDelegate;
        jniUseCustomFilter();
    }

    private void onEvent(int event, String eventMsg) {
        if (mNodePublisherDelegate != null) {
            mNodePublisherDelegate.onEventCallback(this, event, eventMsg);
        }
    }

    private void onCapture(byte[] pictureBuffer, int width, int height, int orientation) {
        if (mCapturePictureListener != null) {
            Bitmap sBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            sBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pictureBuffer));
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            Bitmap dBitmap = Bitmap.createBitmap(sBitmap, 0, 0, width, height, matrix, true);
            sBitmap.recycle();
            mCapturePictureListener.onCaptureCallback(dBitmap);
        }
    }

    public interface CapturePictureListener {
        void onCaptureCallback(Bitmap picture);
    }

    private native long jniInit(Object context, String premium);

    private native void jniDeInit();

    private native int jniInitGPUImage();

    private native int jniChangeGPUImage(int cameraWidth, int cameraHeight, int surfaceWidth, int surfaceHeight);

    private native int jniDrawGPUImage(int textureId);

    private native int jniFreeGPUImage();

    private native int jniSetScaleGPUImage(int zoomScale);

    private native void jniRequestScreenShot();

    private native void jniAudioMuted(boolean pause);

    private native void jniSetAudioParam(int bitrate, int profile, int sampleRate);

    private native void jniUseCustomFilter();

    public native void setVideoParam(int preset, int fps, int bitrate, int profile, boolean frontMirror);

    public native void setAutoReconnectWaitTimeout(int autoReconnectWaitTimeout);

    public native void setConnectWaitTimeout(int connectWaitTimeout);

    public native void setBeautyLevel(int beautyLevel);

    public native void setHwEnable(boolean hwEnable);

    public native void setCryptoKey(String cryptoKey);

    public native void setAudioEnable(boolean audioEnable);

    public native void setVideoEnable(boolean videoEnable);

    public native void setDenoiseEnable(boolean denoiseEnable);

    public native void setDynamicRateEnable(boolean dynamicRateEnable);

    public native void setKeyFrameInterval(int keyFrameInterval);

    public native void setPublishType(int publishType);

    public native int pushRawvideo(byte[] data, int size);

    public native int start();

    public native int stop();


    @Override
    public void OnCreate() {
        if(this.mNodePublisherVideoTextureDelegate != null) {
            this.mNodePublisherVideoTextureDelegate.onCreateTextureCallback(this);
        }
        jniInitGPUImage();
    }

    @Override
    public void OnChange(int cameraWidth, int cameraHeight, int surfaceWidth, int surfaceHeight) {
        this.cameraOri = mNodeCameraView.getCameraOrientation();
        this.surfaceOri = getWindowRotation();
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.isFrontCamera = mNodeCameraView.isFrontCamera();
        if (this.mNodePublisherVideoTextureDelegate != null) {
            this.mNodePublisherVideoTextureDelegate.onChangeTextureCallback(this, this.isFrontCamera, this.cameraOri, this.surfaceOri);
        }
        jniChangeGPUImage(cameraWidth, cameraHeight, surfaceWidth, surfaceHeight);
    }

    @Override
    public void OnDraw(int textureId) {
        if (this.mNodePublisherVideoTextureDelegate != null) {
            textureId = this.mNodePublisherVideoTextureDelegate.onDrawTextureCallback(this, textureId, this.cameraWidth, this.cameraHeight,
                    this.isFrontCamera, this.cameraOri);
        }
        jniDrawGPUImage(textureId);
    }

    @Override
    public void OnDestroy() {
        if(this.mNodePublisherVideoTextureDelegate != null) {
            this.mNodePublisherVideoTextureDelegate.onDestroyTextureCallback(this);
        }
        jniFreeGPUImage();
    }
}

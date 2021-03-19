package cn.nodemedia;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.Surface;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by ALiang on 16/12/15.
 */

public class NodePlayer implements NodePlayerView.RenderCallback {
    static {
        System.loadLibrary("NodeMediaClient");
    }

    private long id;
    private NodePlayerView mNodePlayerView;
    private NodePlayerDelegate mNodePlayerDelegate;
    private static AudioManager.OnAudioFocusChangeListener sAudioFocusChangeListener = null;
    private static List<NodePlayer> players = new ArrayList<>(0);

    private String inputUrl;
    private String pageUrl;
    private String swfUrl;
    private String connArgs;
    private String rtspTransport;

    private int bufferTime;
    private int maxBufferTime;
    private int autoReconnectWaitTimeout;
    private int connectWaitTimeout;
    private int logLevel;
    private boolean hwEnable;
    private boolean audioEnable;
    private boolean videoEnable;
    private boolean subscribe;


    public static final String RTSP_TRANSPORT_UDP = "udp";
    public static final String RTSP_TRANSPORT_TCP = "tcp";
    public static final String RTSP_TRANSPORT_UDP_MULTICAST = "udp_multicast";
    public static final String RTSP_TRANSPORT_HTTP = "http";

    public static final int NM_LOGLEVEL_ERROR = 0;
    public static final int NM_LOGLEVEL_INFO = 1;
    public static final int NM_LOGLEVEL_DEBUG = 2;

    public NodePlayer(@NonNull Context context) {
        this(context, "");
    }

    public NodePlayer(@NonNull Context context, @NonNull String license) {
        this.id = jniInit(context, license);
        this.inputUrl = "";
        this.pageUrl = "";
        this.swfUrl = "";
        this.connArgs = "";
        this.logLevel = NM_LOGLEVEL_ERROR;
        this.rtspTransport = RTSP_TRANSPORT_UDP;
        this.bufferTime = 500;
        this.maxBufferTime = 1000;
        this.autoReconnectWaitTimeout = 2000;
        this.connectWaitTimeout = 0;
        this.hwEnable = true;
        this.audioEnable = true;
        this.videoEnable = true;
        this.subscribe = false;

        if (sAudioFocusChangeListener == null) {
            sAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        for (NodePlayer player : players) {
                            if (player.audioEnable) {
                                player.jniSetAudioEnable(false);
                            }
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        for (NodePlayer player : players) {
                            if (player.audioEnable) {
                                player.jniSetAudioEnable(true);
                            }
                        }
                    }
                }
            };
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.requestAudioFocus(sAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        }
        players.add(this);
    }

    public void release() {
        final NodePlayer self = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                self.mNodePlayerDelegate = null;
                self.mNodePlayerView = null;
                self.jniDeInit();
                self.id = 0;
                players.remove(self);
            }
        }).start();
    }

    public void setInputUrl(@NonNull String inputUrl) {
        this.inputUrl = inputUrl.trim();
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

    public void setRtspTransport(@NonNull String rtspTransport) {
        this.rtspTransport = rtspTransport;
    }

    public void setBufferTime(int bufferTime) {
        this.bufferTime = bufferTime;
    }

    public void setMaxBufferTime(int maxBufferTime) {
        this.maxBufferTime = maxBufferTime;
    }

    public void setHWEnable(boolean hwEnable) {
        this.hwEnable = hwEnable;
    }

    public void setAutoReconnectWaitTimeout(int autoReconnectWaitTimeout) {
        this.autoReconnectWaitTimeout = autoReconnectWaitTimeout;
    }

    public void setConnectWaitTimeout(int connectWaitTimeout) {
        this.connectWaitTimeout = connectWaitTimeout;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public void setAudioEnable(boolean audioEnable) {
        this.audioEnable = audioEnable;
        jniSetAudioEnable(audioEnable);
    }

    public void setVideoEnable(boolean videoEnable) {
        this.videoEnable = videoEnable;
        jniSetVideoEnable(videoEnable);
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }


    public void setPlayerView(@NonNull NodePlayerView npv) {
        mNodePlayerView = npv;
        jniSetVideoEnable(true);
        npv.setRenderCallback(this);
    }

    public void setNodePlayerDelegate(@NonNull NodePlayerDelegate delegate) {
        mNodePlayerDelegate = delegate;
    }

    private void onEvent(int event, String eventMsg) {
        if (mNodePlayerDelegate != null) {
            mNodePlayerDelegate.onEventCallback(this, event, eventMsg);
        }

        if (event == 1104 && mNodePlayerView != null) {
            mNodePlayerView.setVideoSize(Integer.valueOf(eventMsg.split("x")[0]), Integer.valueOf(eventMsg.split("x")[1]));
        }
    }

    private native long jniInit(Object context, String premium);

    private native void jniDeInit();

    private native int jniSetSurface(Object surface);

    private native int jniSetSurfaceChange();

    private native int jniSetAudioEnable(boolean enable);

    private native int jniSetVideoEnable(boolean enable);

    public native int setVolume(float volume);

    public native void setCryptoKey(String cryptoKey);

    public native int start();

    public native int stop();

    public native int pause();

    public native int seekTo(long pos);

    public native long getDuration();

    public native long getCurrentPosition();

    public native long getBufferPosition();

    public native int getBufferPercentage();

    public native boolean isPlaying();

    public native boolean isLive();

    @Override
    public void onSurfaceCreated(@NonNull Surface surface) {
        jniSetSurface(surface);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        jniSetSurfaceChange();
    }

    @Override
    public void onSurfaceDestroyed() {
        jniSetSurface(null);
    }
}

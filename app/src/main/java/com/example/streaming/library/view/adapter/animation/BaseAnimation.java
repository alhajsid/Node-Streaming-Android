package com.example.streaming.library.view.adapter.animation;

import android.animation.Animator;
import android.view.View;

/**
 * Animation基类
 */
public interface BaseAnimation {

    Animator[] getAnimators(View view);
}

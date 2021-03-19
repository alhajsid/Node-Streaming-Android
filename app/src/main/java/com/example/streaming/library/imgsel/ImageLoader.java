package com.example.streaming.library.imgsel;

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

public interface ImageLoader extends Serializable {

    void displayImage(Context context, String path, ImageView imageView);
}
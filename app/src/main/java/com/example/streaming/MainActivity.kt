package com.example.streaming

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_go_live.setOnClickListener {
            startActivity(Intent(this,GoLiveActivity::class.java))

        }

        btn_viewer.setOnClickListener {
            startActivity(Intent(this,ViewerActivity::class.java))
        }

    }

}
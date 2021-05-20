package com.jora.pointrapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jora.pointrapplication.fragments.LogFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logFragment = LogFragment()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeRootFrameLayout, logFragment, "logFragment")
        transaction.commit()
    }
}
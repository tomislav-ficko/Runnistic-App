package com.ficko.runnisticapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ficko.runnisticapp.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var runDAO: RunDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

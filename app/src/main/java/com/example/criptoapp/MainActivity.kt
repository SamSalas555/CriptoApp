package com.example.criptoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var ctext: Button = findViewById<Button>(R.id.ctext_btn)
        var cimg:Button =  findViewById(R.id.cimg_btn)
        ctext.setOnClickListener{
            val intent  = Intent(this, TextActivity::class.java)
            startActivity(intent)
        }
        cimg.setOnClickListener {
            val intent = Intent(this, ImgActivity::class.java)
            startActivity(intent)
        }

    }
}
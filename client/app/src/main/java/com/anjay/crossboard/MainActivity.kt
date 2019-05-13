package com.anjay.crossboard

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast

import java.time.format.SignStyle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var sp:SharedPreferences = applicationContext.getSharedPreferences("pref", Context.MODE_PRIVATE)

        if (!sp.contains("username")){
            setContentView(R.layout.welcome)
            val register:Button = findViewById<Button>(R.id.register_w)
            val signin: Button = findViewById<Button>(R.id.signin_w)

            register.setOnClickListener {
                Toast.makeText(applicationContext,(it as Button).text,Toast.LENGTH_SHORT).show()
                var v:View = layoutInflater.inflate(R.layout.register,null,false)
                v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.abc_slide_in_bottom))
                setContentView(v)
            }
            signin.setOnClickListener {
                Toast.makeText(applicationContext,(it as Button).text,Toast.LENGTH_SHORT).show()
                var v:View = layoutInflater.inflate(R.layout.sign_in,null,false)
                v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.abc_slide_in_bottom))
                setContentView(v)

            }

        }
        else {

        }
    }
}

package com.anjay.crossboard

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import org.json.JSONObject

val tag = "anjay23"

class MainActivity : AppCompatActivity() {
    lateinit var hdlr:Handler
    lateinit var wait:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
       hdlr = Handler()
        super.onCreate(savedInstanceState)

        var sp:SharedPreferences = applicationContext.getSharedPreferences("pref", Context.MODE_PRIVATE)
        var spe:SharedPreferences.Editor=sp.edit()
        if (!(sp.getBoolean("init",false)) || !is_valid_login(sp.getString("email",""),sp.getString("p_hash",""),this)){
                setContentView(R.layout.welcome)
                val registerw:Button = findViewById(R.id.register_w)
                val signinw: Button = findViewById(R.id.signin_w)


                registerw.setOnClickListener {
                    var v:View = layoutInflater.inflate(R.layout.register,null,false)
                    v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.abc_slide_in_bottom))
                    setContentView(v)
                    var register = findViewById<Button>(R.id.sign_up)
                    var username = findViewById<EditText>(R.id.user_name)
                    var email = findViewById<EditText>(R.id.email)
                    email.setOnClickListener{
                        (it as EditText).error = null
                        }


                    var passwd = findViewById<EditText>(R.id.passwd)
                    wait = ProgressDialog(this)

                    register.setOnClickListener {
                        if (username.text.toString()==""){
                            username.error="Please Choose A Username"
                        }
                        else if (email.text.toString()==""){
                            email.error = "Please Provide An Email"
                          }
                         else if (!isValidEmail(email.text.toString())){
                            email.error = "Email Adreess is not valid"
                            return@setOnClickListener
                        }
                        else if (passwd.text.toString()==""){
                            passwd.error = "Please Enter Your Password"
                        }
                        else {
                            wait.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                            wait.setTitle("")
                            wait.setMessage("Please Wait.....")
                            wait.setCanceledOnTouchOutside(false)
                            wait.show()
                            var hm = HashMap<String, String>()
                            hm["username"] = username.text.toString()
                            hm["email"] = email.text.toString()
                            hm["p_hash"] = passwd.text.toString()
                            Thread(Runnable {
                                var msg = request(getString(R.string.server) + "/register", hm, "POST")
                                hdlr.post {
                                    var resp = JSONObject(msg)
                                    wait.setMessage(resp.getString("message"))
                                }

                            }).start()

                        }
                    }


                }
                signinw.setOnClickListener {
                    var v:View = layoutInflater.inflate(R.layout.sign_in,null,false)
                    v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.abc_slide_in_bottom))
                    setContentView(v)
                    var signin:Button =  findViewById(R.id.signin)
                    wait = ProgressDialog(this)


                    signin.setOnClickListener{

                        var email = findViewById<EditText>(R.id.email_l)
                        var passwd = findViewById<EditText>(R.id.passwd_l)
                        if (email.text.toString()==""){
                            email.setError("Please Provide An Email")
                        }
                        if (passwd.text.toString()==""){
                            passwd.setError("Please Enter Ypur Password")

                        }
                        else {
                            var wait = ProgressDialog(this)
                            wait.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                            wait.setTitle("")
                            wait.setMessage("Please Wait.....")
                            wait.setCanceledOnTouchOutside(false)
                            wait.show()
                            var hm = HashMap<String, String>()
                            hm["email"] = email.text.toString()
                            hm["p_hash"] = passwd.text.toString()
                            Thread(Runnable {
                                var msg = request(getString(R.string.server) + "/login", hm, "POST")
                                Log.wtf(tag, msg)
                                var resp = JSONObject(msg)

                                hdlr.post {
                                    if (resp.get("status") == 1) {
                                        spe.putString("username", resp.getString("username"))
                                        spe.putString("email", email.text.toString())
                                        spe.putString("p_hash", passwd.text.toString())
                                        spe.putBoolean("init", true)
                                        spe.commit()
                                        wait.setMessage(resp.getString("message") + "\n Welcome Back " + resp.getString("username"))
                                    }
                                    else {
                                        wait.setMessage(resp.getString("message"))
                                    }
                                }

                            }).start()

                        }
                    }

                }

            }
        else {

        }
    }
    }


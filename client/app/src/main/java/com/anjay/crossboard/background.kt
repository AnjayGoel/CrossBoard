package com.anjay.crossboard

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.json.JSONObject

class background : Runnable, Service() {
    lateinit var sp:SharedPreferences
    lateinit var email:String
    var latest = 0
    lateinit var p_hash:String
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager
    lateinit var hndlr:Handler
    var queued = mutableListOf<String>()

    private fun periodic (){
        Log.wtf(tag,"Looping")
        if (isInternetConnected(this) && isReachable(getString(R.string.server))){
            Log.wtf(tag,"Connected and reachable")
            if (queued.isNotEmpty()){
                Log.wtf(tag,"Not Empty")
                for (str in queued){
                    var hm = HashMap<String, String>()
                    hm.put("data", str)
                    var resp = JSONObject(request(getString(R.string.server) + "/post", hm, "POST", true, email, p_hash))
                    if (resp.getInt("status") == 1){
                        queued.remove(str)
                        latest=resp.getInt("time")
                    }
                }
            }
        }
        hndlr.postDelayed({ periodic() },1000)
    }
    override fun run() {
        Looper.prepare()
        hndlr = Handler()
        sp = getSharedPreferences("pref",Context.MODE_PRIVATE)
        email = sp.getString("email","")
        p_hash = sp.getString("p_hash","")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            var mChannel = NotificationChannel(channelId, "Main", NotificationManager.IMPORTANCE_HIGH)
            NotificationCompat.Builder(this, channelId)
        } else {
            NotificationCompat.Builder(this)
        }
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round).setContentTitle("CrossBoard")
            .setContentText("CrossBoard is Running....").setContentIntent(pendingIntent)
        startForeground(1234, notificationBuilder.build())


        val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cbm.addPrimaryClipChangedListener {
            if (cbm.primaryClip.getItemAt(0).text==null) return@addPrimaryClipChangedListener
            var str = cbm.primaryClip.getItemAt(0).text.toString()
            var hm = HashMap<String, String>()
            hm.put("data", str)
            if (isInternetConnected(this)&& isReachable(getString(R.string.server))) {
                var resp = JSONObject(request(getString(R.string.server) + "/post", hm, "POST", true, email, p_hash))
                if (resp.getInt("status") != 1){
                    queued.add(str)
                }
                else{
                    latest=resp.getInt("time")
                    notificationBuilder.setContentText(str)
                    notificationManager.notify(1234, notificationBuilder.build())
                }
            }
            else{
                queued.add(str)
            }
        }


        hndlr.postDelayed({ periodic() },1000)
        Looper.loop()
    }



    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        Thread(this).start()
        super.onCreate()
    }
}

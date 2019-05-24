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
import android.widget.RemoteViews
import org.json.JSONArray
import org.json.JSONObject

class Background : Runnable, Service() {
    lateinit var file: DataHandler
    lateinit var sp: SharedPreferences
    lateinit var email: String
    lateinit var p_hash: String
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager
    lateinit var hndlr: Handler
    var queued = mutableListOf<String>()

    private fun periodic() {
        if (isInternetConnected(this)) {
            var hm = HashMap<String, String>()
            hm.put("time", "" + file.getLastUpdateTime())
            Log.wtf(tag,"------------>"+file.getLastUpdateTime())
            var str = request(getString(R.string.server) + "/get", hm, "POST", true, email, p_hash)
            var resp = JSONObject(str)
            if (resp.getInt("status")==1){

                var arr = JSONArray(resp.getString("data")).getJSONObject(0).getJSONArray("entry")
                file.put(arr)
            }

            if (queued.isNotEmpty()) {
                Log.wtf(tag, queued.get(0))
                for (str in queued) {
                    var hm = HashMap<String, String>()
                    hm.put("data", str)
                    var resp = JSONObject(request(getString(R.string.server) + "/post", hm, "POST", true, email, p_hash))
                    if (resp.getInt("status") == 1) {
                        queued.remove(str)
                        var obj = JSONObject()
                        obj.put("data", str)
                        obj.put("time", resp.getInt("time"))
                        obj.put("type", resp.getString("type"))
                        file.put(obj)
                    }
                }
            }
        } else {
            Log.wtf(tag, "Not Connected")
        }
        hndlr.postDelayed({ periodic() }, 3000)
    }

    fun updateNotification() {
        return
        //TODO("implement notification update")
    }

    override fun run() {
        Looper.prepare()
        file = DataHandler(this)
        hndlr = Handler()
        sp = getSharedPreferences("pref", Context.MODE_PRIVATE)
        email = sp.getString("email", "")
        p_hash = sp.getString("p_hash", "")

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
        val notifLayout = RemoteViews(packageName,R.layout.notification)
        notificationBuilder.setCustomContentView(notifLayout).setContentIntent(pendingIntent)
        startForeground(1234, notificationBuilder.build())


        val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cbm.addPrimaryClipChangedListener {
            if (cbm.primaryClip.getItemAt(0).text == null) return@addPrimaryClipChangedListener
            var str = cbm.primaryClip.getItemAt(0).text.toString()
            var hm = HashMap<String, String>()
            hm.put("data", str)
            if (isInternetConnected(this)) {
                var resp = JSONObject(request(getString(R.string.server) + "/post", hm, "POST", true, email, p_hash))
                if (resp.getInt("status") != 1) {
                    queued.add(str)
                } else {
                    var obj = JSONObject()
                    obj.put("data", str)
                    obj.put("time", resp.getInt("time"))
                    obj.put("type", resp.getString("type"))
                    file.put(obj)

                    notificationBuilder.setSmallIcon(R.drawable.notification_tile_bg).setContentText(str)
                    notificationManager.notify(1234, notificationBuilder.build())
                }
            } else {
                queued.add(str)
            }
        }


        hndlr.postDelayed({ periodic() }, 1000)
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

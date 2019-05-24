package com.anjay.crossboard

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder

class DataHandler(context: Context) {
    private var con: Context = context
    private var f: File
    private var latestUpdate:Long = 0
    private var arr:JSONArray = JSONArray()
    private var reader: FileReader
    private var writer: FileWriter

    init {
        f = File(con.filesDir, "data")
        writer = FileWriter(f,true)
        reader = FileReader(f)
        reader.forEachLine {
            if (it!="")arr.put(JSONObject(URLDecoder.decode(it,"UTF-8")))
        }
    }

    fun put(obj: JSONObject) {
        Log.wtf(tag,""+obj.getInt("time"))
        if (obj.getLong("time")>latestUpdate) latestUpdate = obj.getLong("time")
        arr.put(obj)
        writer.write("\n"+URLEncoder.encode(obj.toString(4),"UTF-8"))
        writer.flush()
    }

    fun put(array: JSONArray) {
        for (i in 0 until array.length()) {
            put(array.getJSONObject(i))
        }
    }

    fun length():Int{
        return arr.length()
    }
    fun getLastUpdateTime():Long{
       return latestUpdate
    }

    fun get(num: Int=-1):JSONObject? {
       if (num>=arr.length() || num<0)return null
        return arr.getJSONObject(num)
    }
}
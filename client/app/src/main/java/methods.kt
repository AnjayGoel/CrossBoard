package com.anjay.crossboard

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

fun is_valid_login (email:String, p_hash:String, con: Context):Boolean{
    var resp = JSONObject()
    var t = Thread(
        Runnable {
            var hm=  HashMap<String,String>()
            hm.put("email",email)
            hm.put("p_hash",p_hash)
            var msg = request(con.getString(R.string.server)+"/login",hm,"POST")
            resp = JSONObject(msg)
        }
    )
    t.start()
    t.join()
    return resp.getInt("status")==1
}
fun getUrlQuery (dict:HashMap<String,String>?):String {
    if (dict==null)return ""
    var query=""
    for (pair in dict.iterator()){
        query += if (query == "") pair.key+"="+pair.value
        else "&"+pair.key+"="+pair.value
    }
    return query
}
fun isValidEmail(email:String):Boolean{
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
fun request(address:String, params:HashMap<String,String>?,method:String):String{
    var query = getUrlQuery(params)
    var resp=""
    lateinit var url: URL
    if (method=="POST") url = URL(address)
    else if (method=="GET") url = URL(address+"/"+query)
    try {
        var urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.doInput = true
        urlConnection.requestMethod=method
        if (method=="POST") {
            urlConnection.doOutput=true
            var bos = BufferedOutputStream(urlConnection.outputStream)
            var writer = BufferedWriter(OutputStreamWriter(bos))
            writer.write(query)
            writer.flush()
            writer.close()
            bos.close()
        }


        urlConnection.connect()

        var bis= BufferedInputStream(urlConnection.inputStream)
        var reader= BufferedReader(InputStreamReader(bis))
        reader.forEachLine {
            resp+=it
        }
    } catch (e:Exception){
        Log.v("CrossBoard",e.toString())
        e.printStackTrace()
<<<<<<< HEAD
        return "{\"status\":\"2\",\"message\":\"Error Occured: "+e.message+":"+e.cause+"\"}"
=======
        return "{\"status\":\"2\",\"message\":\"Failed to connect\"}"
>>>>>>> parent of 9d28294... Some minor Changes

    }
    Log.wtf("anjay23",resp)
    return resp
}
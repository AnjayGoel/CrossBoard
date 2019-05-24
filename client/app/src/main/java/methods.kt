package com.anjay.crossboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

fun isInternetConnected(context:Context):Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}
fun isReachable (url:String):Boolean{
    val url = URL(url)
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout=2000
    connection.requestMethod="HEAD"
    val code = connection.responseCode

    if (code == 200) {
        return true
    }
    return false
}

fun isValidLogin (email:String, p_hash:String, con: Context):Boolean{
    var resp = JSONObject()
    var t = Thread(
        Runnable {
            var msg = request(con.getString(R.string.server)+"/login",null,"POST",true,email,p_hash)
            resp = JSONObject(msg)
        }
    )
    t.start()
    t.join()
    Log.wtf(tag,resp.toString())
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
fun request(address:String, params:HashMap<String,String>?,method:String,basic_auth:Boolean=false,email: String="",p_hash: String=""):String{

    var query = getUrlQuery(params)
    var resp=""
    lateinit var url: URL
    if (method=="POST") url = URL(address)
    else if (method=="GET") url = URL(address+"/"+query)
    try {

        var urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.doInput = true
        urlConnection.requestMethod=method
        if (basic_auth){
            var auth_string = "Basic " + String(Base64.encode((email+":"+p_hash).toByteArray(),Base64.DEFAULT))
            urlConnection.setRequestProperty("Authorization",auth_string)
        }
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
        Log.wtf(tag,e.toString())
        e.printStackTrace()
        return "{\"status\":\"2\",\"message\":\"Error Occurred: "+e.message+":"+e.cause+"\"}"

    }
    Log.wtf(tag, "Response is $resp")
    return resp
}
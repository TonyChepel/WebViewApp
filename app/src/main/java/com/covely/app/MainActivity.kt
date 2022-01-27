package com.covely.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AFInAppEventType
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerConversionListener
import com.covely.app.databinding.ActivityMainBinding
import com.covely.entity.InstallEntity
import com.covely.entity.RequestPushToken
import com.covely.entity.UserEntity
import com.covely.retrofit.ApiRetrofit
import com.covely.retrofit.RetfofitInstace
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var adapter : ImageAdapter? = null
    var tokenFS : String? = ""
    val id = AppsFlyerLib.getInstance().getAppsFlyerUID(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        installPostUser()
        webViewConfig()
        initialRcAdapter()

    }

    private fun initialRcAdapter() = with(binding){
        adapter = ImageAdapter(ImageManager.listImage,this@MainActivity)
        rcView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
        rcView.adapter = adapter
    }


    private fun installPostUser(){
     val conversionListener : AppsFlyerConversionListener = object : AppsFlyerConversionListener{
         override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {


             val status : String = Objects.requireNonNull(conversionData!!["af_status"]).toString()

             if (Objects.requireNonNull(conversionData["is_first_launch"]).toString() == "true"){

              permission()

              val time =TimeZone.getDefault()
                 if(status == "Non-organic") {
                     lifecycleScope.launch(Dispatchers.IO) {
                         val idfa = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)?.id
                         val adId =
                             if (conversionData.containsKey("adgroup_id")) conversionData["adgroup_id"].toString() else "" //conversionData["adgroup_id"].toString()
                         val campaign =
                             if (conversionData.containsKey("campaign")) conversionData["campaign"].toString() else "" //conversionData["campaign"].toString()
                            RetfofitInstace.api.install(
                             UserEntity(
                                 user_id = id,
                                 timezone = time.id,
                                 app_package = "com.covely.app",
                                 idfa = idfa,
                                push_token =  tokenFS,
                                 ad_campaign = campaign,
                                 ad_id = adId
                             )
                         )

                         FirebaseMessaging.getInstance().token.addOnCompleteListener(
                             OnCompleteListener { task ->
                             if (!task.isSuccessful) {
                                 return@OnCompleteListener
                             }
                             // Get new FCM registration token
                             tokenFS = task.result
                             try {
                                 lifecycleScope.launch {
                                     installsPushToken(id, tokenFS!!)
                                 }
                             } catch (e : Exception){
                                 Log.e("Exception", e.toString())
                             }
                         })
                     }
                 } else {
                     lifecycleScope.launch(Dispatchers.IO) {
                     try {
                      RetfofitInstace.api.install(UserEntity(id,time.id,"com.covely.app"))
                     }catch(e:Exception){

                     }
                     }

                 }


             } else {
                 FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                     if (!task.isSuccessful) {
                         return@OnCompleteListener
                     }
                     // Get new FCM registration token
                     tokenFS = task.result

                     try {
                         lifecycleScope.launch(Dispatchers.IO) {
                             try {
                                  RetfofitInstace.api.installUser(id)
                                 if (tokenFS != RetfofitInstace.api.installUser(id).entity.push_token)
                                     tokenFS?.let {
                                          RetfofitInstace.api.installUserIdPushToken(id, RequestPushToken(it))

                                     }
                                 else Log.e("Push", "Not Changed")
                                 //api.testPush(id) //todo comment it
                             }
                             catch (e: Exception) {
                                 Log.e("Exception", e.toString())

                             }
                             //installsPushToken(id,tokenFB!!)
                         }
                     } catch (e: Exception) {
                         Log.e("Exception", e.toString())

                     }

                 })
             }

         }

         override fun onConversionDataFail(p0: String?) {

         }

         override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

         }

         override fun onAttributionFailure(p0: String?) {

         }

     }
        AppsFlyerLib.getInstance().init("rZs38Yax3tAvktqELJ5UW5", conversionListener, applicationContext)
        AppsFlyerLib.getInstance().start(this)
    }

    suspend fun installsPushToken(userId: String, pushToken: String){
        try{
             RetfofitInstace.api.installUserIdPushToken(userId, RequestPushToken(pushToken))
        }
        catch (e: Exception) {
            Log.e("Exception", e.toString())

        }
    }

    fun permission(){
        Log.e("permission","here")
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(this,arrayOf(
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ),1)
        }
    }



    private fun webViewConfig() = with(binding) {

        val cashDir = applicationContext.filesDir.absolutePath + "cache/"
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setAppCacheEnabled(true)
        settings.allowFileAccess = true
        settings.setAppCacheMaxSize(1024 * 1024 * 8)
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setAppCachePath(cashDir)
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.saveFormData = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.allowUniversalAccessFromFileURLs = true


        lifecycleScope.launchWhenCreated {
            val response = try {
                RetfofitInstace.api.installUser(id)
            }catch (e:Exception){
               return@launchWhenCreated
            }
            Log.d("GoTo","${response.goto}")
            if(response.allow) {
                webView.webViewClient = object : WebViewClient() {

                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null) {
                            view?.loadUrl(url)
                        }
                        return true

                    }
                }

                webView.webChromeClient = object : WebChromeClient(){
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if(newProgress == 100){
                            pbBarWeb.visibility = View.GONE
                        }
                        else {
                            pbBarWeb.visibility = View.VISIBLE
                        }
                    }
                }

                webView.loadUrl(response.goto.toString())

            } else {
                webView.visibility = View.GONE
                pbBarWeb.visibility = View.GONE
            }
        }


    }
}
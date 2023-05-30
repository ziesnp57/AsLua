package com.yongle.aslua.login

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.tencent.connect.UserInfo
import com.tencent.mmkv.MMKV
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.yongle.aslua.MainActivity
import org.json.JSONObject

open class BaseUiListener(private val mTencent: Tencent) : DefaultUiListener() {
    private val kv = MMKV.defaultMMKV()
    override fun onComplete(response: Any?) {
        if (response == null) {
            "返回为空,登录失败".showToast()
            return
        }
        val jsonResponse = response as JSONObject
        if (jsonResponse.length() == 0) {
            "返回为空,登录失败".showToast()
            return
        }
        kv.encode("qq_login", response.toString())


        doComplete(response)
        getQQInfo(response)


        val intent = Intent(MainActivity.context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(MainActivity.context, intent, null)

    }

    private fun doComplete(values: JSONObject?) {
        val gson = Gson()
        val qqLogin = gson.fromJson(values.toString(), QQLogin::class.java)
        mTencent.setAccessToken(qqLogin.access_token, qqLogin.expires_in.toString())
        mTencent.openId = qqLogin.openid
    }

    override fun onError(e: UiError) {

    }

    override fun onCancel() {
    }

    //获取qq信息
    private fun getQQInfo(respons: Any?) {
        val qqToken = mTencent.qqToken
        val info = UserInfo(MainActivity.context, qqToken)

        //获取用户信息
        info.getUserInfo(object : BaseUiListener(mTencent) {
            override fun onComplete(response: Any?) {
                kv.encode("qq_info", response.toString())
                val gson = Gson()
                val qqInfo = gson.fromJson(response.toString(), QQInfo::class.java)
                val qqLogin = gson.fromJson(respons.toString(), QQLogin::class.java)

                //注册用户
                userlogin(qqLogin.openid, qqInfo.nickname, qqInfo.figureurl_qq_2)
            }
        })



    }


}

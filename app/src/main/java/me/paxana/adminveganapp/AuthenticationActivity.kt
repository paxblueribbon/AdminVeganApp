package me.paxana.adminveganapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonaws.mobile.client.*

class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        AWSMobileClient.getInstance().initialize(applicationContext, object:
            Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?) {
                when (result?.userState) {
                    UserState.SIGNED_IN -> {
                        val i = Intent(this@AuthenticationActivity, MainActivity::class.java)
                        startActivity(i)
                    }
                    UserState.SIGNED_OUT -> showSignIn()
                    else -> {
                        AWSMobileClient.getInstance().signOut()
                        showSignIn()
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("err", e?.message)
            }

        })
    }

    private fun showSignIn(){
        //TODO: [DESIGN] CUSTOM BACKGROUND ON LOGIN SCREEN
        try{
            AWSMobileClient.getInstance().showSignIn(this, SignInUIOptions.builder().nextActivity(
                MainActivity::class.java).build())
        }
        catch(e: Exception) {
            Log.e("Err", e.message)
        }
    }
}

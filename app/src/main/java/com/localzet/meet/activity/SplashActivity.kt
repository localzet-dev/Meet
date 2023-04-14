package com.localzet.meet.activity

import com.localzet.meet.R
import com.localzet.meet.sharedpref.AppPref
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppPref.isAppIntroShown) {
            if (resources.getBoolean(R.bool.enable_mandatory_authentication)) {
                // Authentication is mandatory
                if (FirebaseAuth.getInstance().currentUser != null) {
                    // Intro shown and user authenticated
                    MainActivity.startActivity(this)
                    finish()
                } else {
                    // Intro shown but user unauthenticated
                    AuthenticationActivity.startActivity(this)
                    finish()
                }
            } else {
                // Authentication is optional. Intro shown but user unauthenticated
                MainActivity.startActivity(this)
                finish()
            }
        } else {
            AppIntroActivity.startActivity(this)
            finish()
        }
    }
}

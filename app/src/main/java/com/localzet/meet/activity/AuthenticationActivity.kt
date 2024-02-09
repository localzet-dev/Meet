/**
 * @package     Localzet Meet
 * @link        https://meet.localzet.com
 * @link        https://github.com/localzet-dev/Meet
 *
 * @author      Ivan Zorin <creator@localzet.com>
 * @copyright   Copyright (c) 2018-2024 Zorin Projects S.P.
 * @license     https://www.gnu.org/licenses/agpl-3.0 GNU Affero General Public License v3.0
 *
 *              This program is free software: you can redistribute it and/or modify
 *              it under the terms of the GNU Affero General Public License as published
 *              by the Free Software Foundation, either version 3 of the License, or
 *              (at your option) any later version.
 *
 *              This program is distributed in the hope that it will be useful,
 *              but WITHOUT ANY WARRANTY; without even the implied warranty of
 *              MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *              GNU Affero General Public License for more details.
 *
 *              You should have received a copy of the GNU Affero General Public License
 *              along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *              For any questions, please contact <creator@localzet.com>
 */

package com.localzet.meet.activity

import com.core.extensions.makeGone
import com.core.extensions.makeVisible
import com.core.extensions.toast
import com.localzet.meet.R
import com.localzet.meet.databinding.ActivityAuthenticationBinding
import com.localzet.meet.sharedpref.AppPref
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse

class AuthenticationActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, AuthenticationActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private val rcSignIn = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showSkipSignIn()

        onSignInWithGoogleClick()
        onSignInWithEmailClick()
        onSkipClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // RC_SIGN_IN is the request code you passed when starting the sign in flow.
        if (requestCode == rcSignIn) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                AppPref.isUserAuthenticated = true
                MainActivity.startActivity(this)
                finish()
                return
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    toast(getString(R.string.authentication_login_canceled))
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    toast(getString(R.string.all_error_internet_connectivity))
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    toast(getString(R.string.all_error_unknown))
                    return
                }
            }
        }
    }

    /**
     * Sets the visibility of the chipSkip based on the mandatory authentication configuration
     */
    private fun showSkipSignIn() {
        if (resources.getBoolean(R.bool.enable_mandatory_authentication))
            binding.chipSkip.makeGone() else binding.chipSkip.makeVisible()
    }

    private fun onSignInWithGoogleClick() {
        binding.btnSignInGoogle.setOnClickListener {
            startSignInFlow(AuthUI.IdpConfig.GoogleBuilder().build())
        }
    }

    private fun onSignInWithEmailClick() {
        binding.btnSignInEmail.setOnClickListener {
            startSignInFlow(AuthUI.IdpConfig.EmailBuilder().build())
        }
    }

    private fun onSkipClick() {
        binding.chipSkip.setOnClickListener {
            MainActivity.startActivity(this)
            finish()
        }
    }

    private fun startSignInFlow(idpConfig: AuthUI.IdpConfig) {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        idpConfig
                    )
                )
                .setTheme(R.style.AppTheme)
                .setIsSmartLockEnabled(false)
                .build(),
            rcSignIn
        )
    }
}

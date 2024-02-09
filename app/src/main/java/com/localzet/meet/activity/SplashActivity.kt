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

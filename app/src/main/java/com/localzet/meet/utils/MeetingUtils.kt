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

package com.localzet.meet.utils

import com.core.extensions.toast
import com.localzet.meet.R
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.google.firebase.auth.FirebaseAuth
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL

object MeetingUtils {

    fun startMeeting(context: Context, meetingCode: String, @StringRes initialToastMessage: Int) {
        context.toast(context.getString(initialToastMessage))

        val serverUrl = URL("https://meet.jit.si")
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            .setWelcomePageEnabled(false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("live-streaming.enabled", true)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(meetingCode)
            .setUserInfo(null)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userInfoBundle = if (currentUser != null) {
            bundleOf(
                "displayName" to currentUser.displayName,
                "email" to currentUser.email,
                "avatarURL" to currentUser.photoUrl
            )
        } else {
            bundleOf(
                "displayName" to "Гость"
            )
        }

        options.setUserInfo(JitsiMeetUserInfo(userInfoBundle))
        JitsiMeetActivity.launch(context, options.build())
    }
}
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
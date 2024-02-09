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

import com.core.extensions.*
import com.localzet.meet.Meetly
import com.localzet.meet.R
import com.localzet.meet.databinding.ActivityMainBinding
import com.localzet.meet.model.Meeting
import com.localzet.meet.sharedpref.AppPref
import com.localzet.meet.utils.MeetingUtils
import com.localzet.meet.viewmodel.MainViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import coil.api.load
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.navigationInfoParameters
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val viewModel by viewModel<MainViewModel>() // Lazy inject ViewModel
    private lateinit var binding: ActivityMainBinding

    private val minMeetingCodeLength = 10
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser
        setProfileIcon()

        handleDynamicLink()

        onMeetingToggleChange()
        onCreateMeetingCodeChange()
        onCopyMeetingCodeFromClipboardClick()
        onShareMeetingCodeClick()
        onJoinMeetingClick()
        onCreateMeetingClick()
        onMeetingHistoryClick()
        onProfileClick()
    }

    private fun setProfileIcon() {
        currentUser?.let {
            if (it.photoUrl != null) {
                binding.ivProfile.load(currentUser?.photoUrl) {
                    placeholder(R.drawable.ic_profile)
                }
            }
        }
    }

    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    deepLink?.getQueryParameter("meetingCode")?.let { joinMeeting(it) }
                }
            }
            .addOnFailureListener { _ ->
                toast(getString(R.string.main_error_fetch_dynamic_link))
            }
    }

    /**
     * Called when the meeting toggle button check state is changed
     */
    private fun onMeetingToggleChange() {
        binding.tgMeeting.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnToggleJoinMeeting   -> {
                        binding.groupCreateMeeting.makeGone()
                        binding.groupJoinMeeting.makeVisible()
                    }
                    R.id.btnToggleCreateMeeting -> {
                        binding.groupJoinMeeting.makeGone()
                        binding.groupCreateMeeting.makeVisible()
                        val meetingCode = generateMeetingCode()
                        binding.etCodeCreateMeeting.setText(meetingCode)
                    }
                }
            }
        }
    }

    /**
     * Called when the meeting code in the EditText of the CREATE MEETING toggle changes
     */
    private fun onCreateMeetingCodeChange() {
        binding.tilCodeCreateMeeting.etCodeCreateMeeting.doOnTextChanged { text, _, _, _ ->
            if (text.toString().trim()
                    .replace(" ", "").length >= minMeetingCodeLength
            ) binding.tilCodeCreateMeeting.error = null
        }
    }

    private fun generateMeetingCode(): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Called when the clipboard icon is clicked in the EditText of the JOIN MEETING toggle
     */
    private fun onCopyMeetingCodeFromClipboardClick() {
        binding.tilCodeJoinMeeting.setEndIconOnClickListener {
            val clipboardText = getTextFromClipboard()
            if (clipboardText != null) {
                binding.etCodeJoinMeeting.setText(clipboardText)
                toast(getString(R.string.main_meeting_code_copied))
            } else {
                toast(getString(R.string.main_empty_clipboard))
            }
        }
    }

    /**
     * Called when the share icon is clicked in the EditText of the CREATE MEETING toggle
     */
    private fun onShareMeetingCodeClick() {
        binding.tilCodeCreateMeeting.setEndIconOnClickListener {
            if (isMeetingCodeValid(getCreateMeetingCode())) {
                binding.tilCodeCreateMeeting.error = null
                toast("Генерируется ссылка")

                Firebase.dynamicLinks.shortLinkAsync {
                    link = Uri.parse("https://meet.localzet.com/?meetingCode=" + getCreateMeetingCode())
                    domainUriPrefix = "https://localzetmeet.page.link"
                    androidParameters("com.localzet.meet") {
                        fallbackUrl = link
                    }

                    navigationInfoParameters {
                        forcedRedirectEnabled = true // Directly open the link in the app
                    }
                }.addOnSuccessListener { result ->
                    val shortDynamicLink = result.shortLink.toString()
                    startShareTextIntent(
                        getString(R.string.main_share_meeting_code_title),
                        getString(R.string.main_share_meeting_code_desc, shortDynamicLink)
                    )
                }.addOnFailureListener {
                    toast(getString(R.string.main_error_create_dynamic_link))
                }
            } else {
                binding.tilCodeCreateMeeting.error =
                    getString(R.string.main_error_meeting_code_length, minMeetingCodeLength)
            }
        }
    }

    /**
     * Called when the JOIN button is clicked of the JOIN MEETING toggle
     */
    private fun onJoinMeetingClick() {
        binding.btnJoinMeeting.setOnClickListener {
            if (isMeetingCodeValid(getJoinMeetingCode())) {
                joinMeeting(getJoinMeetingCode())
            }
        }
    }

    private fun joinMeeting(meetingCode: String) {
        MeetingUtils.startMeeting(
            this,
            meetingCode,
            R.string.all_joining_meeting
        ) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                meetingCode,
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    /**
     * Returns the meeting code for joining the meeting
     */
    private fun getJoinMeetingCode() =
        binding.etCodeJoinMeeting.text.toString().trim().replace(" ", "")

    /**
     * Called when the CREATE button is clicked of the CREATE MEETING toggle
     */
    private fun onCreateMeetingClick() {
        binding.btnCreateMeeting.setOnClickListener {
            if (isMeetingCodeValid(getCreateMeetingCode())) {
                createMeeting(getCreateMeetingCode())
            }
        }
    }

    private fun createMeeting(meetingCode: String) {
        MeetingUtils.startMeeting(
            this,
            meetingCode,
            R.string.all_creating_meeting
        ) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                meetingCode,
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    private fun getCreateMeetingCode() =
        binding.etCodeCreateMeeting.text.toString().trim().replace(" ", "")

    private fun isMeetingCodeValid(meetingCode: String): Boolean {
        return if (meetingCode.length >= minMeetingCodeLength) {
            true
        } else {
            Snackbar.make(
                binding.constrainLayout,
                getString(R.string.main_error_meeting_code_length, minMeetingCodeLength),
                Snackbar.LENGTH_SHORT
            ).show()
            false
        }
    }

    private fun onMeetingHistoryClick() {
        binding.ivMeetingHistory.setOnClickListener {
            MeetingHistoryActivity.startActivity(this)
        }
    }

    private fun onProfileClick() {
        binding.ivProfile.setOnClickListener {
            val profileDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                customView(R.layout.dialog_profile)
            }

            profileDialog.apply {
                if (currentUser != null) {
                    currentUser!!.photoUrl?.let {
                        ivUserProfileDialog.load(it) {
                            placeholder(R.drawable.ic_profile)
                        }
                    }
                    tvUserName.text = currentUser!!.displayName
                    tvEmail.text = currentUser!!.email
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_out)
                } else {
                    tvUserName.makeGone()
                    tvEmail.makeGone()
                    tvUserNotAuthenticated.makeVisible()
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_in)
                }

                switchDarkMode.isChecked = !AppPref.isLightThemeEnabled

                // UserAuthenticationStatus button onClick
                btnUserAuthenticationStatus.setOnClickListener {
                    dismiss()

                    if (currentUser != null) {
                        // User is currently signed in
                        AuthUI.getInstance().signOut(this@MainActivity).addOnCompleteListener {
                            AuthenticationActivity.startActivity(this@MainActivity)
                            finish()
                        }
                    } else {
                        // User is not signed in
                        AuthenticationActivity.startActivity(this@MainActivity)
                        finish()
                    }
                }

                // Dark Mode Switch
                switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                    dismiss()

                    // Change theme after dismiss to prevent memory leak
                    onDismiss {
                        if (isChecked) setThemeMode(AppCompatDelegate.MODE_NIGHT_YES) else setThemeMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                }

                // Privacy Policy onClick
                tvPrivacyPolicy.setOnClickListener {
                    openUrl("https://www.localzet.com/privacy", R.color.colorSurface)
                }

                // Terms of Service onClick
                tvTermsOfService.setOnClickListener {
                    openUrl("https://www.localzet.com/terms", R.color.colorSurface)
                }
            }
        }
    }

    private fun setThemeMode(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        AppPref.isLightThemeEnabled = themeMode == AppCompatDelegate.MODE_NIGHT_NO
    }
}

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

package com.core.extensions


import com.core.R
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback


/**
 * Relaunches the current activity
 */
fun Context.relaunchActivity(context: Activity, intent: Intent) {
    context.finish()
    startActivity(intent)
}

/**
 * Shows the toast message
 */
fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

/**
 * Opens the passed URL in the Chrome Custom Tabs
 */
fun Context.openUrl(url: String, @ColorRes toolbarColor: Int) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .setToolbarColor(ContextCompat.getColor(this, toolbarColor))
        .setShowTitle(true)
        .build()

    // This is optional but recommended
    CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent)

    CustomTabsHelper.openCustomTab(
        this,
        customTabsIntent,
        Uri.parse(url),
        WebViewFallback() // Opens in system browser if Chrome isn't installed on device
    )
}

fun Context.copyTextToClipboard(textToCopy: String, textCopiedMessage: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Copied Text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    toast(textCopiedMessage)
}

fun Context.getTextFromClipboard(): CharSequence? {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text
}

fun Context.startEmailIntent(toEmail: String, emailSubject: String?, emailBody: String = "") {
    // Open Email app with subject and to field pre-filled
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        type = "message/rfc822"
        val uriText = String.format(
            "mailto:%s?subject=%s&body=%s",
            toEmail, emailSubject, emailBody
        )
        data = Uri.parse(uriText)
    }

    // Check if an Email app is installed on the device
    if (emailIntent.resolveActivity(packageManager) != null) {
        startActivity(emailIntent)
    } else {
        toast(getString(R.string.text_no_email_app_found))
    }
}

/**
 * Starts an Intent for sharing text
 */
fun Context.startShareTextIntent(shareTitle: String, shareText: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    startActivity(Intent.createChooser(shareIntent, shareTitle))
}

/**
 * Starts an Intent for sharing an image
 */
fun Context.startShareImageIntent(uri: Uri) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/*"
    }
    // Launch sharing dialog for image
    startActivity(Intent.createChooser(shareIntent, "Share Image"))
}

/**
 * Start an Intent for sharing multiple images
 */
fun Context.startShareImageIntent(uriList: ArrayList<Uri>) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
    }

    startActivity(Intent.createChooser(shareIntent, getString(R.string.text_share_all_images)))
}

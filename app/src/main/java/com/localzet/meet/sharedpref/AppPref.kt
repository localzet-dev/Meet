package com.localzet.meet.sharedpref

import com.chibatching.kotpref.KotprefModel

object AppPref : KotprefModel() {

    var isAppIntroShown by booleanPref(false)
    var isUserAuthenticated by booleanPref(false)

    var isLightThemeEnabled by booleanPref(true)
}
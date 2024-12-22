package com.macwap.rdxrasel.untils


object ConstructionID {


    const val NEWS_TAG              = "NEWS"
    const val DEFAULT_TAG           = "DEFAULT"
    const val PROFILE_T_DEFAULT     = "1"
    internal val USER_PATTERN_SPACE = "<a href=\"user://id=(.*?)&name=(.*?)\">(.*?)</a>:".toRegex()
    internal val USER_PATTERN  	    = "<a href=\"user://id=(.*?)&name=(.*?)\">(.*?)</a>".toRegex()
    internal val FAKE_USER_PATTERN  = "<a href=\"user://id=(.*?)&name=(.*?)\"></a>".toRegex()


}
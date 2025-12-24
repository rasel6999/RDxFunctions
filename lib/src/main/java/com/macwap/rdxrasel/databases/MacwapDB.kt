@file:Suppress("DEPRECATION", "unused", "SpellCheckingInspection")
package com.macwap.rdxrasel.databases

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager

object MacwapDB {

	@JvmStatic
	fun getBoolean(context: Context?, key: String?): Boolean {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getBoolean(key, false)
	}

	@JvmStatic
    fun getString(context: Context?, key: String?): String {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getString(key, "") ?: ""
	}

	@JvmStatic
	fun Context.stringGet(key: String?): String {
		return getString(this, key)
	}
	@JvmStatic
	fun Context.stringSet (key: String?, value: String?) {
		setString(this,key,value )
 	}

	@JvmStatic
	fun getUid(context: Context?): String {
		return getString(context, "user_id")
	}
	@JvmStatic
	fun Context.getUserId(): String {
		return getString(this, "user_id")
	}
	fun Context.getUserIdInt(): Int {
		return getUserId().toIntOrNull()?:0
	}
	@JvmStatic
	fun Context.getSession(): String {
		return getString(this, "user_app_session")
	}
	@JvmStatic
	fun Context.getUserLang(): String {
		return getString(this, "user_lang")
	}

	@JvmStatic
	fun setUid(context: Context?, uId: String?) {
		setString(context, "user_id", uId)
	}
	@JvmStatic
	fun getLang(context: Context?): String {
		return getString(context, "user_lang")
	}

	@JvmStatic
	fun setLang(context: Context?, lang: String?) {
		setString(context, "user_lang", lang)
	}
	@JvmStatic
	fun getCountry(context: Context?): String {
		return getString(context, "user_country")
	}
	@JvmStatic
	fun Context.getUserCountry(): String {
		return getString(this, "user_country")
	}

	@JvmStatic
	fun Context.getCountryName(): String {
		return getString(this, "userCountryName")
	}

	@JvmStatic
	fun Context.setCountryName(country: String?) {
		setString(this, "userCountryName", country)
	}


	@JvmStatic
	fun setCountry(context: Context?, country: String?) {
		setString(context, "user_country", country)
	}
	@JvmStatic
	fun getInt(context: Context?, key: String?): Int {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getInt(key, -1)
	}

	@JvmStatic
	fun Context.intGet(key: String?): Int {
		return getInt(this,key)
	}
	@SuppressLint("CommitPrefEdits")
	@JvmStatic
	fun setString(context: Context?, key: String?, value: String?) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
	}
	@JvmStatic
	fun setBoolean(context: Context?, key: String?, value: Boolean) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value)
			.apply()
	}
	@JvmStatic
	fun getSuperString(context: Context?, key: String?): String {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getString(key, "0") ?: "0"

	}
	@JvmStatic
	fun setInt(context: Context?, key: String?, value: Int) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
	}
	@JvmStatic
	fun Context.intSet(key: String?, value: Int) {
		setInt(this,key,value)
	}


	@JvmStatic
	fun getSuperInt(context: Context?, key: String?): Int {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getInt(key, 0)
	}
	@JvmStatic
	fun setLong(context: Context?, key: String?, value: Long?) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value!!).apply()
	}
	@JvmStatic
	fun getSuperLong(context: Context?, key: String?): Long {
		val preferences = PreferenceManager.getDefaultSharedPreferences(context)
		return preferences.getLong(key, 0L)
	}
}
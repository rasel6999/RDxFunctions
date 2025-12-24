@file:Suppress("DEPRECATION")    
@file:RequiresApi(Build.VERSION_CODES.HONEYCOMB)

package com.macwap.rdxrasel.untils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.macwap.rdxrasel.R
import com.macwap.rdxrasel.databases.MacwapDB
import com.macwap.rdxrasel.databases.MacwapDB.stringGet
import com.macwap.rdxrasel.silicon.SiliconWebView
import com.macwap.rdxrasel.untils.ConstructionID.USER_PATTERN
import com.macwap.rdxrasel.untils.ConstructionID.USER_PATTERN_SPACE
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack
import java.util.regex.Pattern
import androidx.core.net.toUri
import com.macwap.rdxrasel.shimmer.Shimmer
import com.macwap.rdxrasel.shimmer.ShimmerDrawable

@Suppress("unused")
object FunctionManager {
    fun Int.withAlpha(alpha: Float): Int {
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        return Color.argb(
            alphaInt,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }

    fun Resources.dp(value: Int): Int =
        (value * this.displayMetrics.density).toInt()

    fun View.setTextViewText(id: Int, ledgerString: String) {
        findViewById<TextView>(id)?.text = ledgerString
    }

    fun String.isNumeric(): Boolean {
        return all { it.isDigit() }
    }

    fun Any.escapeSql(): String {
        return this.toString().replace("'", "''")
    }

    fun JSONObject.getJsonString(key: String): String? {
        return if (has(key) && !isNull(key) && getString(key).isNotEmpty()) {
            getString(key)
        } else {
            null
        }
    }

    @JvmStatic
    fun isItemFullyVisible(recyclerView: RecyclerView, position: Int): Boolean {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return false

        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if (position in firstVisiblePosition..lastVisiblePosition) {
            val view = layoutManager.findViewByPosition(position) ?: return false
            val visibleRect = android.graphics.Rect()
            if (view.getGlobalVisibleRect(visibleRect)) {
                return view.height == visibleRect.height() && view.width == visibleRect.width()
            }
        }
        return false
    }

    @JvmStatic
    fun Activity.alert(string:String?){
        Toast.makeText(this, "$string", Toast.LENGTH_SHORT).show()
    }
    @JvmStatic
    fun Context.alert(string:String?){
        Toast.makeText(this, "$string", Toast.LENGTH_SHORT).show()
    }
    @JvmStatic
    fun safeStringToInt(input: String): Int? {
        // Attempt to convert to Int safely
        val result = input.toIntOrNull()
        if (result != null) {
            println("Conversion successful: $result")
        } else {
            println("Conversion failed, not a valid integer.")
        }
        return result
    }

    @JvmStatic
    fun String.isInt(): Boolean {
        return toIntOrNull() != null
    }

    @JvmStatic
    fun String.loadBaseUrl(mWebView: SiliconWebView?, baseUrl:String) {
        val decodedHtmlContent = "<!DOCTYPE html> $this"

        mWebView?.loadDataWithBaseURL(
            baseUrl,
            decodedHtmlContent,
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )
    }

    fun String.getUrlId(query: Int): String? {

        val url = URL(this)

        val domain = url.host // Outputs: macwap.com
        val pathSegments = url.path.split("/").filter { it.isNotEmpty() } // ["hashtag", "futuretech"]

        if(query==101){ return domain }

        return pathSegments.getOrNull(query)
    }
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @JvmStatic
    fun View.viewRotateAnimation(duration: Long, i: Long, isCounterClockwise: Boolean) {
        val targetRotation = if (isCounterClockwise) -360f else 360f


        val rotateAnimation = ObjectAnimator.ofFloat(this, "rotation", 0f, targetRotation)
        rotateAnimation.duration = i
        rotateAnimation.repeatCount = ObjectAnimator.INFINITE // Optional: Repeat infinitely
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.start()

        postDelayed({
            rotateAnimation.cancel()
        }, duration)
    }
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @JvmStatic
    fun View.viewZoomAnimation(duration: Long = 500L) {
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.5f, 1f) // Scale to 50% and back
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.5f, 1f) // Scale to 50% and back

        // Set duration and interpolator for smooth animation
        scaleX.duration = duration // 5 seconds
        scaleY.duration = duration
        scaleX.interpolator = LinearInterpolator()
        scaleY.interpolator = LinearInterpolator()

        // Play animations together
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.start()
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun View.viewGoneAnimation(duration: Long = 300L, onComplete: (() -> Unit)? = null) {
        animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                visibility = View.GONE
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
                onComplete?.invoke()
            }
            .start()
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun View.viewVisibleAnimation(duration: Long = 300L, onComplete: (() -> Unit)? = null) {
        visibility = View.VISIBLE
        scaleX = 0f
        scaleY = 0f
        alpha = 0f

        animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                onComplete?.invoke()
            }
            .start()
    }
    @JvmStatic
    fun String.getQueryParams(query: String?): String? {
        if (isNullOrEmpty()) return null

        val uri = Uri.parse(this) //

        return uri.getQueryParameter(query)
    }


    @JvmStatic
    fun Toolbar.setAppBarEnterAlways(isEnabled: Boolean) {
        val params = layoutParams as AppBarLayout.LayoutParams

        if (isEnabled) {
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            params.scrollFlags = 0
        }

        layoutParams = params
    }

    @JvmStatic @SuppressLint("ClickableViewAccessibility")
    fun RecyclerView.prefixScroll() {
        setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }
    }

    @JvmStatic
    fun log( tag: String? = "RDX://", value: String) {
        Log.d(tag,value)
    }
    @JvmStatic
    fun Context.progressDialog(txt: String? = null): Dialog {
        val dialog = Dialog(this)
        val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        txt?.let {  inflate?.findViewById<TextView>(R.id.textLoading)?.text = txt }
        dialog.setContentView(inflate)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        return dialog
    }

    @JvmStatic
    fun loadCustomTabForSite(context: Context?, url: String, color: Int = Color.RED) {
        var mCustomTabsSession: CustomTabsSession? = null
        val mCustomTabsServiceConnection: CustomTabsServiceConnection?
        var mClient: CustomTabsClient?
        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                mClient = customTabsClient
                mClient?.warmup(0L)
                mCustomTabsSession = mClient?.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mClient = null
            }
        }
        CustomTabsClient.bindCustomTabsService(
            context!!,
            "com.android.chrome",
            mCustomTabsServiceConnection
        )

        val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
            .setToolbarColor(color)
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(context, Uri.parse(url))


    }

    @JvmStatic
    fun String.logger(type: String? = "") {
        if (type == "") {
            Log.d("RDX://", this)
        }else{
            Log.d(type, this)
        }

    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic @Suppress("LocalVariableName", "unused")
    fun getRelationTime(time: Long): String {
         val IN_MILLIS = DateUtils.DAY_IN_MILLIS * 30

        val now = Date().time
        val delta = now - time
        val resolution: Long = when {
            delta <= DateUtils.MINUTE_IN_MILLIS -> {
                DateUtils.SECOND_IN_MILLIS
            }
            delta <= DateUtils.HOUR_IN_MILLIS -> {
                DateUtils.MINUTE_IN_MILLIS
            }
            delta <= DateUtils.DAY_IN_MILLIS -> {
                DateUtils.HOUR_IN_MILLIS
            }
            delta <= DateUtils.WEEK_IN_MILLIS -> {
                DateUtils.DAY_IN_MILLIS
            }
            else -> return when {
                delta <= IN_MILLIS -> {
                    (delta / DateUtils.WEEK_IN_MILLIS).toInt().toString() + " weeks ago"
                }
                delta <= DateUtils.YEAR_IN_MILLIS -> {
                    (delta / IN_MILLIS).toInt().toString() + " months ago"
                }
                else -> {
                    (delta / DateUtils.YEAR_IN_MILLIS).toInt().toString() + " years ago"
                }
            }
        }
        return DateUtils.getRelativeTimeSpanString(time, now, resolution).toString()
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun parseDate(time: Long): String {

      var date =  getRelationTime(time)
        date = date.replace("(.*.)second ago".toRegex(), "Now")
        date = date.replace("(.*.)seconds ago".toRegex(), "Now")
        date = date.replace(" minutes ago".toRegex(), "m")
        date = date.replace(" minute ago".toRegex(), "m")
        date = date.replace(" hours ago".toRegex(), "h")
        date = date.replace(" hour ago".toRegex(), "h")
        date = date.replace(" days ago".toRegex(), "d")
        date = date.replace(" day ago".toRegex(), "d")
        date = date.replace(" months ago".toRegex(), "M")
        date = date.replace(" month ago".toRegex(), "M")
        date = date.replace(" month(.*?)ago".toRegex(), "M")
        date = date.replace(" year(.*?)ago".toRegex(), "Y")

        date = date.replace(" week(.*?)ago".toRegex(), "w")
        date = date.replace("(.*?)yesterday(.*?)".toRegex(), "1d")
        date = date.replace("(.*.)In(.*.)".toRegex(), "Now")
        date = date.replace("(.*.)in(.*.)".toRegex(), "Now")
        date = date.replace("(.*?)Yesterday(.*?)".toRegex(), "1d")


        return  date

    }

    @JvmStatic
    fun loadJSONFromAsset(activity: Activity, filename: String): String? {
        return try {
            val `is` = activity.assets.open("layout/$filename")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val charset: Charset = Charsets.UTF_8
            String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun getDateInMillis(srcDate: String): String {
         val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val time1 = srcDate.subTimestamp().toInt()
            val date = Date(time1 * 1000L)
            val millis = date.time
            val timePassedString = DateUtils.getRelativeTimeSpanString(
                millis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS
            )



            return timePassedString.toString()
        }
        return ""
    }

    @JvmStatic
    fun String.subTimestamp(): String {
        return if (this == "") {
            "0"
        } else {
            substring(0,10)
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun getDateInMilliBN(srcDate: String, type: String): String {

        var text: String
        text = if (type == "1") {
            getDateInMillis(srcDate)
        } else {
            srcDate
        }

        //region translate bangle date and time
        if (type == "2") {
            text = text.replace("January".toRegex(), "জানুয়ারী")
            text = text.replace("February".toRegex(), "ফেব্রুয়ারী")
            text = text.replace("March".toRegex(), "মার্চ")
            text = text.replace("April".toRegex(), "এপ্রিল")
            text = text.replace("May".toRegex(), "মে")
            text = text.replace("June".toRegex(), "জুন")
            text = text.replace("July".toRegex(), "জুলাই")
            text = text.replace("August".toRegex(), "আগস্ট")
            text = text.replace("September".toRegex(), "সেপ্টেম্বর")
            text = text.replace("October".toRegex(), "অক্টোবর")
            text = text.replace("November".toRegex(), "নভেম্বর")
            text = text.replace("December".toRegex(), "ডিসেম্বর")
            text = text.replace("AM".toRegex(), "সকাল")
            text = text.replace("PM".toRegex(), "বিকাল")
            text = text.replace("am".toRegex(), "সকাল")
            text = text.replace("pm".toRegex(), "বিকাল")
        }
        text = text.replace("yesterday".toRegex(), "গতকাল")
        text = text.replace("Yesterday".toRegex(), "গতকাল")
        text = text.replace("minute ago".toRegex(), "মিনিট পুর্বে ")
        text = text.replace("minutes ago".toRegex(), "মিনিট পুর্বে ")
        text = text.replace("hour ago".toRegex(), "ঘণ্টা পুর্বে ")
        text = text.replace("hours ago".toRegex(), "ঘণ্টা পুর্বে ")
        text = text.replace("day ago".toRegex(), "দিন পুর্বে ")
        text = text.replace("days ago".toRegex(), "দিন পুর্বে ")
        text = text.replace("month ago".toRegex(), "মাস পুর্বে ")
        text = text.replace("months ago".toRegex(), "মাস পুর্বে ")
        text = text.replace("year ago".toRegex(), "বছর পুর্বে ")
        text = text.replace("years ago".toRegex(), "বছর পুর্বে ")
        text = text.replace("hour".toRegex(), "ঘণ্টা পুর্বে ")
        text = text.replace("minutes".toRegex(), "মিনিট পুর্বে ")
        text = text.replace("day".toRegex(), "দিন পুর্বে ")
        text = text.replace("days".toRegex(), "দিন পুর্বে ")
        text = text.replace("month".toRegex(), "মাস পুর্বে ")
        text = text.replace("months".toRegex(), "মাস পুর্বে ")
        text = text.replace("year".toRegex(), "বছর পুর্বে ")
        text = text.replace("years".toRegex(), "বছর পুর্বে ")
        text = text.replace("In".toRegex(), "")
        text = text.replace("in".toRegex(), "")
        text = text.replace("s".toRegex(), "")
        text = text.replace("1".toRegex(), "১")
        text = text.replace("2".toRegex(), "২")
        text = text.replace("3".toRegex(), "৩")
        text = text.replace("4".toRegex(), "৪")
        text = text.replace("5".toRegex(), "৫")
        text = text.replace("6".toRegex(), "৬")
        text = text.replace("7".toRegex(), "৭")
        text = text.replace("8".toRegex(), "৮")
        text = text.replace("9".toRegex(), "৯")
        text = text.replace("0".toRegex(), "০")


        //endregion
        return text
    }

    @JvmStatic @SuppressLint("SimpleDateFormat")
    fun getDateAndTime(srcDate: String): String? {
        val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val t = srcDate.toInt()
            val date = Date(t * 1000L)
            val formatter = SimpleDateFormat("dd MMMM yyyy,  hh:mm a")
            return formatter.format(date)
        }
        return ""
    }

    @JvmStatic @SuppressLint("SimpleDateFormat")
    fun getDate(srcDate: String): String? {
        val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val t = srcDate.toInt()
            val date = Date(t * 1000L)
            val formatter = SimpleDateFormat("dd MMMM yyyy")
            return formatter.format(date)
        }
        return ""
    }

    @JvmStatic @SuppressLint("SimpleDateFormat")
    fun getTime(srcDate: String): String? {
        val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val t = srcDate.toInt()
            val date = Date(t * 1000L)
            val formatter = SimpleDateFormat("hh:mm a")
            return formatter.format(date)
        }
        return ""
    }

    @JvmStatic
    fun openLink(context: Context, url: String?) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }

    @JvmStatic
    fun html2text(html: String?): String {
        return Jsoup.parse(html!!).text()
    }

    @JvmStatic
    fun getColor(context: Context, colorResId: Int): Int {

        //return ContextCompat.getColor(context, colorResId); // Doesn't seem to work for R.attr.colorPrimary
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    @JvmStatic
    fun Context.getColorRes(colorResId: Int): Int {
        return ContextCompat.getColor(this, colorResId)

    }

    @JvmStatic
    fun Context.getAttrColor(colorResId: Int): Int {

        val typedValue = TypedValue()
        theme.resolveAttribute(colorResId, typedValue, true)

        return ContextCompat.getColor(this, typedValue.resourceId)

    }

    @JvmStatic
    fun removeLastComma(text: String?): String {

        val reText =  text?.replace("^(,|\\s)*|(,|\\s)*$".toRegex(), "")
            ?.replace("(,\\s*)+".toRegex(), ",")

        if(reText !=null) return reText

        return  ""
    }

    @JvmStatic
    fun removeLastOR(text: String?): String {

        val reText = text?.replace("^(OR |\\s)*|(OR |\\s)*$".toRegex(), "")
            ?.replace("(OR \\s*)+".toRegex(), "OR ")

        if(reText !=null) return reText

        return  ""
    }

    @JvmStatic
    fun orToArray(txt: String?): Array<String?> {

        return txt!!.split(" OR ".toRegex()).toTypedArray()

    }

    @JvmStatic
    fun splitOR(txt: String?): String {

        val text = orToArray(txt)
        var mainText =""
        for (i in text.indices) {
            mainText += "\"${text[i]}\" OR "
        }

        return removeLastOR(mainText)

    }

    @JvmStatic
    fun shareText(context: Context, text: String?, title: String?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(sharingIntent, title))
    }


    @JvmStatic
    fun setHtml(text: String?): CharSequence {
        return if (text != null) {
            val snap: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(text)
            }
            snap
        } else {
            ""
        }
    }


    @JvmStatic
    fun Spanned.getHtml(): String {

    var text=    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        HtmlCompat.toHtml(this, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
    } else {
        Html.toHtml(this)
    }
        text=  text?.replace(USER_PATTERN_SPACE) { "[url=user://${it.groupValues[1]}]"+setHtml(it.groupValues[2])+"[/url]"}
        text=  text?.replace(USER_PATTERN) { "[url=user://${it.groupValues[1]}]"+setHtml(it.groupValues[2])+"[/url]"}

        text= setHtml(text).toString()

        return (text)
    }

    @JvmStatic
    fun setHtml(text: CharSequence?): CharSequence {
        return if (text != null) {
            val snap: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text.toString(), Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(text.toString())
            }
            snap
        } else {
            ""
        }
    }

    @JvmStatic
    fun setHtmlImageGetter(text: String?, imageGetter: Html.ImageGetter): CharSequence {
        return if (text != null) {
            val snap: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT,imageGetter,null)
            } else {
                @Suppress("DEPRECATION") Html.fromHtml(text,imageGetter,null)
            }
            snap
        } else {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    @JvmStatic
    fun capitalizeWords(string: String?): String {
      return string?.split(",")?.joinToString(",") { it.capitalize(Locale.ROOT) }.toString()
    }

    @JvmStatic
    fun articleBBCode(html: String?): String? {
        var bbCode = html
         bbCode = bbCode?.replace("\\r\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t".toRegex(), "")
        bbCode = bbCode?.replace("\\r\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t".toRegex(), "")
        bbCode = bbCode?.replace("\\r\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\r\\n\\t\\t\\t\\t\\t\\t".toRegex(), "")
        bbCode = bbCode?.replace("\\r\\n\\t\\t\\t\\t\\t".toRegex(), "")

        bbCode = bbCode?.replace("<img".toRegex(), "<img onclick=\"FullscreenIMG(this.src)\" ")

        return bbCode
    }

    @JvmStatic
    fun searchText(text: String?): String {
        var text1 = text
        text1 = text1?.replace(" {3}".toRegex(), " ")
        text1 = text1?.replace(" {2}".toRegex(), " ")
        text1 = text1?.replace(" ".toRegex(), " OR ")

        text1 =removeLastOR(text1)
         return text1
    }

    @JvmStatic
    fun removeSpecialCharacters(text: String): String {
        var text1 = text
        text1 = text1.replace("-".toRegex(), " ")

        text1 =removeLastOR(text1)
        return text1
    }

    @JvmStatic
    fun getDomain(url: String): String {
        var returnText=""
        when (url) {
            "", "about:blank" -> {
            }
            else -> {

                val startIndex: Int = url.indexOf("//") + 2


                returnText = url.indexOf('/', startIndex).let { url.substring(startIndex, it) }
            }
        }

        return returnText
    }

    @JvmStatic
    fun shimmerDrawable(baseColor:Int? = null, highLightColor:Int? = null ,duration:Long?  = null ): ShimmerDrawable {
        baseColor?.let {
            highLightColor?.let { it1 ->
                if (duration != null) {
                    Shimmer.ColorHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                        .setDuration(duration) // how long the shimmering animation takes to do one full sweep
                        .setBaseColor(it)
                        .setHighlightColor(it1)
                        .setDropoff(50f)
                        .setBaseAlpha(1f) //the alpha of the underlying children
                        .setHighlightAlpha(1f) // the shimmer alpha amount
                        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                        .setAutoStart(true)
                        .build()
                }
            }
            return ShimmerDrawable().apply { shimmer = shimmer }

        }


        Shimmer
            .ColorHighlightBuilder()
            .setHighlightAlpha(1f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        return ShimmerDrawable().apply { }
    }

    @JvmStatic
    fun setupTheme(context: Context) {

        when (MacwapDB.getString(context,"default_theme")) {
            "day" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            }
            "night" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            }
            "default" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            }
            else -> {
                when {
                    context.stringGet("defaultTheme")=="day" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                    }
                    context.stringGet("defaultTheme")=="night" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                    }
                    else ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

                    }
                }

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    @JvmStatic
    fun isNightMode(context:Context):String{

     return   when (MacwapDB.getString(context,"default_theme")) {
         "day" ->   "day"
         "night" ->  "night"
         else ->
             when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                 Configuration.UI_MODE_NIGHT_NO ->      "day"
                 Configuration.UI_MODE_NIGHT_YES ->     "night"
                else ->                                 "unknown"
             }
     }
    }

    @JvmStatic
    fun openExternalBrowser(activity: Activity?, url: String?) {
        val uri = url?.toUri()
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        activity?.startActivity(goToMarket)

    }

    @JvmStatic
    fun String.removeAt():String {
      return  this.replace("@", "")

    }

    @JvmStatic
    fun RecyclerView?.getCurrentPosition() : Int {
        return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    @JvmStatic
    fun decodeCategory(category: String,index: Int): String {
        val items = removeLastComma(category.replace("@", "")).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex()).toTypedArray()

        if(items.size>index)
            if(items[index] != "")

                return  "%@${items[index]}@%"

        return "@fakeId@"

    }

    @JvmStatic
    fun ImageView.loadColiImage(url: String?,context: Context, placeholder:Int,error:Int) {

        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(500)
            .diskCachePolicy(CachePolicy.ENABLED)
            .placeholder(placeholder)
            .error(error)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun hideKeyboard(activity: Activity?,view: View?){

        val manager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view?.windowToken, 0)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun Activity.showKeyboard(view: View?){
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
     }

    @SuppressLint("ObsoleteSdkInt")
    @JvmStatic @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.transparentStatusBar( ints: Int,backgroundInt:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            val background = ContextCompat.getDrawable(this, backgroundInt)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this,android.R.color.transparent)
            if (ints == 1) {
                window.navigationBarColor = ContextCompat.getColor(this,android.R.color.transparent)
            }
            window.setBackgroundDrawable(background)
        }
    }

    @JvmStatic @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.statusBarColor(statusBarColor: Int, backgroundColor:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
            window.setBackgroundDrawable(ColorDrawable(backgroundColor))
        }
    }

    @JvmStatic
    fun Drawable.drawableTint(tintColor: Int): Drawable {
        val wrappedDrawable =  DrawableCompat.wrap(this)
        DrawableCompat.setTint(wrappedDrawable, tintColor)
        return this
    }

    @JvmStatic
    fun String.webContent(encode:Boolean = true) :String{
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">" +
                "<style> img {  max-width: 100%; max-height: 200px; }</style>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />" +
                "</head><body style=\"background-color:transparent\">"+if(encode) setHtml(this) else "$this</body></html>"
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @SuppressLint("SuspiciousIndentation")
    @JvmStatic
    fun View.setAnimate(duration: Int){
        this.alpha = 0.0f

            this.animate()
                .setDuration(duration.toLong())
                .alpha(1.0f)
                .setListener(object : AnimatorListenerAdapter() { })
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @JvmStatic
    fun View.setOutAnimate(duration: Int){

        this.animate()
            .setDuration(duration.toLong())
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() { })
    }
    @JvmStatic
    fun textToArray(txt: String?): Array<String?> {

        return txt!!.split(",".toRegex()).toTypedArray()

    }

    @JvmStatic
    fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
            .toInt()
    }

    @JvmStatic
    fun String.htmlEncode() :String{
        var text:String = this
      //  text =  TextUtils.htmlEncode(text)
        text =  text.replace( "+", "&plus;")

        return text
    }

    @JvmStatic
    fun String.toTypedArrayInt(): IntArray {
        val text = removeLastComma(this)
        val tokens = text.split(",".toRegex()).toTypedArray()
        val numbers = IntArray(tokens.size)
        for (i in tokens.indices) {
            numbers[i] = tokens[i].toInt()
        }
        return numbers
    }

    @JvmStatic
    fun shortIntArray(intArray: IntArray, lengths: Int) {
        var temp: Int
        for (counter in 0 until lengths - 1) {
            for (index in 0 until lengths - 1 - counter) {
                if (intArray[index] < intArray[index + 1]) {
                    temp = intArray[index]
                    intArray[index] = intArray[index + 1]
                    intArray[index + 1] = temp
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @JvmStatic
    fun Context.downloadRequest(url: String?, suggestedFilename: String?) {

        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, suggestedFilename)
        val dm: DownloadManager = getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()

    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @JvmStatic
    fun Context.downloadRequestImage(url: String, folderName: String?="Media", fileName: String?="") {
        val fileName1 = if(fileName=="")
            url.substring(url.lastIndexOf('/') + 1, url.length)
        else fileName

        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM , "$folderName/$fileName1")

        val dm: DownloadManager = getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()

    }


    @JvmStatic
    fun deleteFiles(file: File,day: Int){
        val diff: Long = Date().time - file.lastModified()

        if (diff > day * 24 * 60 * 60 * 1000) {
            file.delete()
        }
    }

    @JvmStatic
    fun getAllFilesInDir(dir: File?): ArrayList<File>? {
        if (dir == null) return null
        val files = ArrayList<File>()
        val dirList = Stack<File>()
        dirList.clear()
        dirList.push(dir)
        while (!dirList.isEmpty()) {
            val dirCurrent = dirList.pop()
            val fileList = dirCurrent.listFiles()
            for (aFileList in fileList) {
                if (aFileList.isDirectory) dirList.push(aFileList) else  if(aFileList.extension !="nomedia")
                    files.add(aFileList)
            }
        }
         return files
    }

    @JvmStatic
    fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        if ( layoutParams is ViewGroup.MarginLayoutParams) {
            val p: ViewGroup.MarginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            requestLayout()
        }
    }

    @JvmStatic
    fun String.shareUrlRewrite():String {
        var url = this
        url =  url. replace(" ", "_")
            .replace("\n\n\n".toRegex(), "_")
            .replace("\n\n".toRegex(), "_")
            .replace("\n".toRegex(), "_")
            .replace("/n".toRegex(), "_")
            .replace(" {3}".toRegex(), " ")
            .replace(" {2}".toRegex(), " ")
            .replace(" {2}".toRegex(), " ")
            .replace(" ".toRegex(), "_")
            .replace("'".toRegex(), "")
            .replace("\"".toRegex(), "")
            .replace(" ", "_")
        url = url.replace(" ", "_")
        url = url.replace("/", "_")
        url = url.replace("\\", "_")
        return url //urlEncoder(this)
    }

    @JvmStatic
    fun String.isNumber(): Boolean {
        return if (isNullOrEmpty()) false else all { Character.isDigit(it) }
    }

    fun Uri.fileUriSystem(): String? {
        val reString = this
        return if (Build.VERSION.SDK_INT >= 29) {

            reString .toString()

        }else{
            reString.path

        }

    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun onlineSystem(time: String): String {
        return when {
            isActiveOn5minute(time) -> {
                "online"
            }
            isActive1hour(time) -> {
                "1hour"
            }
            else -> {
                "offline"
            }
        }
    }

    @JvmStatic
    fun isActiveOn5minute(srcDate: String): Boolean {
        val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val time2 = srcDate.toInt()
            val date = Date(time2 * 1000L)
            val inMillis = date.time
            DateUtils.getRelativeTimeSpanString(
                inMillis,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            )
            val osTime = (10 * 60 * 1000).toLong()
            val differenceInMillis = System.currentTimeMillis() - inMillis
            return osTime >= differenceInMillis
        }
        return false
    }
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @JvmStatic
    fun isActive1hour(srcDate: String): Boolean {
        val isNumber = Pattern.matches("[0-9]+", srcDate)
        if (isNumber) {
            val time2 = srcDate.toInt()
            val date = Date(time2 * 1000L)
            val inMillis = date.time
            DateUtils.getRelativeTimeSpanString(
                inMillis,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            )
            val osTime = (60 * 60 * 1000).toLong()
            val differenceInMillis = System.currentTimeMillis() - inMillis
            return osTime >= differenceInMillis
        }
        return false
    }
    @JvmStatic
    fun readableType(text: String?): String {
        return if (text == "url") {
            "link"
        } else {
            "Posts"
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    fun setBorder(context: Context, tab: LinearLayout, width: Int, colorResId: Int, removePadding: Boolean = false) {
        val border = GradientDrawable()
        border.setStroke(width, getColor(context, colorResId))
        val layers = arrayOf<Drawable>(border)
        val layerDrawable = LayerDrawable(layers)
        layerDrawable.setLayerInset(0, 1, -width, -width, -width)
        tab.background = layerDrawable
        if (removePadding)
            tab.setPadding(0, 0, 0, 0)
        else
            tab.setPadding(10, 5, 0, 0)

    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @JvmStatic
    fun copyText(context: Context, text: String?): Boolean{
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.text = text
        return true
    }

    @JvmStatic
    fun Context.toast(message: CharSequence,short: Boolean) {
        if(short)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    }

    @JvmStatic
    fun isJson(text: String?): Boolean {
        val m = Pattern.compile("\\{*.}").matcher(text!!)
        while (m.find()) {
            return true
        }
        return false
    }

    @JvmStatic
    fun setMargin(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p: ViewGroup.MarginLayoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }
    @RequiresApi(Build.VERSION_CODES.FROYO)
    @JvmStatic
    fun BottomSheetDialogFragment.setTransparentBackground() {
        dialog?.apply {
            setOnShowListener {
                val bottomSheet = findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)

            }
        }
    }
    @JvmStatic
    fun String.getBaseUrl(): String {
        if (isNotEmpty())
        return try {
            val url = URL(this)
            "${url.protocol}://${url.host}/"
        } catch (e: Exception) {
            ""
        }
        return ""
    }
    @JvmStatic
    fun String.encodeUrl(): String {
        return URLEncoder.encode(this, "UTF-8")
    }

    fun urlEncoder(link: String?): String? {
        var s: String? = null
        try {
            s = URLEncoder.encode(link, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return s
    }

    fun bbCode(bbCode: String): String {
        var bbCode1 = bbCode
        bbCode1 = bbCode1.replace("\\n".toRegex(), "<br/>")
        bbCode1 = bbCode1.replace("\\n".toRegex(), "<br/>")
        bbCode1 = bbCode1.replace("&lt;em&gt;".toRegex(), "")
        bbCode1 = bbCode1.replace("&lt;/em&gt;".toRegex(), "")
        bbCode1 = bbCode1.replace("&lt;p".toRegex(), "&lt;div")
        bbCode1 = bbCode1.replace("&lt;/p&gt;".toRegex(), "&lt;/div&gt;")
        bbCode1 = bbCode1.replace("&lt;blockquote&gt;".toRegex(), "")
        bbCode1 = bbCode1.replace("&lt;/blockquote&gt;".toRegex(), "")
         bbCode1 = bbCode1.replace("https://".toRegex(), "http://")
        bbCode1 = bbCode1.replace("&lt;a".toRegex(), "&lt;p")
        bbCode1 = bbCode1.replace("&lt;/a&gt;".toRegex(), "&lt;/p&gt;")
         bbCode1 = bbCode1.replace("<img".toRegex(), "<img onclick=\"FullscreenIMG(this.src)\" ")

         return bbCode1
    }

    fun bbcode(html: String, activity: Activity?): String {
        var html1 = html
        var userName: String = MacwapDB.getString(activity, "user_name")
        userName = if (userName == "null" || userName == "" || userName == "1") {
            "Guest"
        } else {
            userName
        }
        html1 = html1.replace(":user:".toRegex(), userName)
        html1 = html1.replace(
            ":on-promotion:".toRegex(),
            MacwapDB.getString(activity, "on-promotion")
        )
        html1 = html1.replace(
            ":off-promotion:".toRegex(),
            MacwapDB.getString(activity, "off-promotion")
        )
        html1 = html1.replace(
            ":on-notification:".toRegex(),
            MacwapDB.getString(activity, "on-notification")
        )
        html1 = html1.replace(
            ":off-notification:".toRegex(),
            MacwapDB.getString(activity, "off-notification")
        )
        html1 =
            html1.replace(":linkColor:".toRegex(), MacwapDB.getString(activity, "link-color"))
        html1 =
            html1.replace(":textColor:".toRegex(), MacwapDB.getString(activity, "text-color"))
        html1 = html1.replace(
            ":classic-style:".toRegex(),
            MacwapDB.getString(activity, "classic-style")
        )
        html1 = html1.replace(
            ":light-style:".toRegex(),
            MacwapDB.getString(activity, "light-style")
        )
        html1 =
            html1.replace(":dark-style:".toRegex(), MacwapDB.getString(activity, "dark-style"))
        html1 = html1.replace(
            ":macwap-style:".toRegex(),
            MacwapDB.getString(activity, "macwap-style")
        )
        html1 = html1.replace("<a".toRegex(), "<a onclick=\"CallDuty(this.href,'xWebView')\" ")
        html1 = html1.replace(
            ":color-primary:".toRegex(),
            MacwapDB.getString(activity, "color-primary") + ""
        )
        html1 =
            "<script type=\"text/javascript\">   function CallDuty(url,type) {   Call.CallForDuty(url,type); }</script>$html1"
        html1 =
            "<script type=\"text/javascript\">   function CallDutyMore(url,type,id) {   Call.CallForDutyNew(url,type,id); }</script>$html1"
        html1 =
            "<script type=\"text/javascript\">function HideMePlease(e){[].forEach.call(document.querySelectorAll(\".\"+e),function(e){e.style.visibility=\"hidden\"})}</script>$html1"
        return html1
    }

    fun playSound(context: Context, _id: Int) {
        try {
            val notification: Uri = Uri.parse("android.resource://" + context.packageName + "/" + _id) //Here is FILE_NAME is the name of file that you want to play
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("HardwareIds")
    fun getDeviceId(activity: Activity): String {
        val deviceId: String = try {
            Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            "Not Found"
        }
        return deviceId
    }


    fun alertBox(message: String?,activity: Activity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage(Html.fromHtml(message))
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(activity.resources.getString(R.string.ok)) { _, _ -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



    fun isNetworkAvailable(activity: Activity): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {


        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }
    }

}
package com.fh.payday.utilities

import android.graphics.PorterDuff
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import java.text.ParseException
import java.text.SimpleDateFormat


/**
 * PayDayFH
 * Created by EResolute on 10/30/2018.
 */
fun TextView.setDrawableTopTint(
        @DrawableRes drawableRes: Int,
        @ColorRes colorRes: Int = R.color.white
) {
    var drawable = ContextCompat.getDrawable(this.context, drawableRes) ?: return
    drawable = DrawableCompat.wrap(drawable)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null)
    } else {
        this.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    val color = ContextCompat.getColor(this.context, colorRes)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(drawable, color)
    } else {
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun TextView.setDrawableLeftTint(
        @DrawableRes drawableRes: Int,
        @ColorRes colorRes: Int = R.color.white
) {
    var drawable = ContextCompat.getDrawable(this.context, drawableRes) ?: return
    drawable = DrawableCompat.wrap(drawable)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    } else {
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    val color = ContextCompat.getColor(this.context, colorRes)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(drawable, color)
    } else {
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun TextView.setTextWithUnderLine(text: String) {
    val spanText = SpannableString(text)
    spanText.setSpan(UnderlineSpan(), 0, text.length, 0)
    setText(spanText)
}

fun TextView.setTextColorSpan(text: String, @ColorInt color: Int) {
    val spannable = SpannableString(text).apply {
        setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    setText(spannable)
}

fun TextView.addHypen(input: String?) {

    val text = input ?: return
    val builder = StringBuilder()
    for (i in 0 until text.length) {
        if (i == 2) {
            builder.append("-")
            builder.append(text[i])
        } else {
            builder.append(text[i])
        }
        setText(builder)
    }
    setText(builder)
}

fun TextView.removeHypen(text: String): String {
    return text.replace("-", "")
}

fun getClickableSpan(
        text: String,
        clickableText: String,
        @ColorInt color: Int,
        action: () -> Unit,
        isUnderlineText: Boolean = true
): Spannable {
    val clickableSpan = object : ClickableSpan() {

        override fun onClick(widget: View) {
            action()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = isUnderlineText
            ds.color = color
        }
    }

    val spannable = SpannableString("$text $clickableText")
    spannable.setSpan(clickableSpan, text.length, clickableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

fun formatDate(dateString: String): String {

    var finalDateString = ""

//    2018-08-09T14:27:23Z

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    try {

        val convertedDate = dateFormat.parse(dateString)
        Log.i("TextViewUtils", convertedDate.toString())


        val sdfnewformat = SimpleDateFormat("dd/MM/yyyy")
        finalDateString = sdfnewformat.format(convertedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
        Log.i("TextViewUtils", e.message)
    }

    return finalDateString
}

fun TextView.replaceZero(string: String) {
    if (string.isEmpty()) {
        text = ""
        return
    }

    if (string[0] == '0') {
        text = "+971-${string.trimStart('0')}"
    } else {
        text = string
    }
}

fun ArrayList<*>.filterValue(list: ArrayList<*>, number: String) {
    filter { list.contains(number) }

}

fun setDrawableEnd(textView: TextView, icon: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, icon, 0)
    } else {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
    }
}

class MaxLinesToggleClickListener(private val collapsedLines: Int) : View.OnClickListener {
    private val transition = ChangeBounds().apply {
        duration = 200
        interpolator = FastOutSlowInInterpolator()
    }

    override fun onClick(view: View) {
        TransitionManager.beginDelayedTransition(findParent(view), transition)
        val textView = view as TextView
        textView.maxLines = if (textView.maxLines > collapsedLines) collapsedLines else Int.MAX_VALUE
    }

    private fun findParent(view: View): ViewGroup {
        var parentView: View? = view
        while (parentView != null) {
            val parent = parentView.parent as View?
            if (parent is RecyclerView) {
                return parent
            }
            parentView = parent
        }
        // If we reached here we didn't find a RecyclerView in the parent tree, so lets just use our parent
        return view.parent as ViewGroup
    }
}

fun isEllipsized(layout: Layout): Boolean {
    val lines = layout.lineCount

    if(lines > 0) {
        val ellipsisCount = layout.getEllipsisCount(lines - 1)
        if (ellipsisCount > 0) {
            return true
        }
    }

    return false
}
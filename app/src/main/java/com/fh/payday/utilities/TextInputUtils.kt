package com.fh.payday.utilities

import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.res.ResourcesCompat
import android.text.*
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.fh.payday.R
import java.util.regex.Pattern


fun TextInputLayout.setErrorMessage(errorMessage: String) {
    error = errorMessage
}

fun TextInputLayout.clearErrorMessage() {
    error = null
    isErrorEnabled = false
}

fun TextView.removeErrorMessage() {
    visibility = View.GONE
    text = null
}

fun TextView.addErrorMessage(errorMessage: String) {
    visibility = View.VISIBLE
    text = errorMessage
}

fun ConstraintLayout.onErrorMsg() {
    background = ResourcesCompat.getDrawable(resources, R.drawable.bg_border_error, null)
}

fun ConstraintLayout.clearErrorMsg() {
    background = ResourcesCompat.getDrawable(resources, R.drawable.bg_blue_rounded_border, null)
}

fun EditText.setMaxLength(length: Int) {
    val filterArray = arrayOfNulls<InputFilter>(1)
    filterArray[0] = InputFilter.LengthFilter(length)
    filters = filterArray
}

inline fun EditText.onTextChanged(
        crossinline block: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit
) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            block(s, start, before, count)
        }
    })
}


class CardFormatWatcher : TextWatcher {
    private val space = ' '

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]
            if (space == c) {
                s.delete(s.length - 1, s.length)
            }
        }

        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]

            if (Character.isDigit(c) && TextUtils.split(s.toString(), space.toString()).size <= 3) {
                s.insert(s.length - 1, space.toString())
            }
        }
    }
}

class DecimalDigitsInputFilter(lengthLeft: Int, lengthRight: Int) : InputFilter {
    private val regex = "[0-9]{0,${lengthLeft - 1}}+((\\.[0-9]{0,${lengthRight - 1}})?)||(\\.)?".toRegex()

    override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
    ): CharSequence? {
//        if (dest?.matches(regex) == true)
//            return null
//
//        return ""

        val len = end - start
        val string = dest?.split(".") ?: return null
        if (string.isEmpty()) return null
        if (string[0].length > 8) {
            return ""
        }

        if (string.size == 1) return null

        if (string[1].length >= 2 && string[0].isNotEmpty() && string[0].length > 8) {
            return ""
        }
        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source
        }

        val dLen = dest.length

        // Find the position of the decimal .
        for (i in 0 until dstart) {
            if (dest[i] == '.') {
                // being here means, that a number has
                // been inserted after the dot
                // check if the amount of digits is right
                return if ((dLen - (i + 1) + len) > 2)
                    ""
                else
                    SpannableStringBuilder(source, start, end)
            }
        }

        for (i in start until end) {
            if (source!![i] == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                return if (dLen - dend + (end - (i + 1)) > 2)
                    ""
                else
                    break  // return new SpannableStringBuilder(source, start, end);
            }
        }

        // if the dot is after the inserted part,
        // nothing can break
        return null
    }
}

class DecimalDigitsInputFilter3(
        private val lengthLeft: Int, private val lengthRight: Int
) : InputFilter {
    override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
    ): CharSequence? {

        val len = end - start
        val string = dest?.split(".") ?: return null
        if (string.isEmpty()) return null

        if (string[0].length > lengthLeft - 1) {
            return ""
        }

        if (string.size == 1) return null

        if (string[1].length >= lengthRight && string[0].isNotEmpty() && string[0].length > lengthLeft - 1) {
            return ""
        }
        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source
        }

        val dLen = dest.length

        // Find the position of the decimal .
        for (i in 0 until dstart) {
            if (dest[i] == '.') {
                // being here means, that a number has
                // been inserted after the dot
                // check if the amount of digits is right
                return if ((dLen - (i + 1) + len) > lengthRight)
                    ""
                else
                    SpannableStringBuilder(source, start, end)
            }
        }

        for (i in start until end) {
            if (source!![i] == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                return if (dLen - dend + (end - (i + 1)) > lengthRight)
                    ""
                else
                    break  // return new SpannableStringBuilder(source, start, end);
            }
        }

        // if the dot is after the inserted part,
        // nothing can break
        return null
    }
}

class DecimalDigitsInputFilter2(digitsBeforeDecimal: Int, digitsAfterDecimal: Int) : InputFilter {
    private val mPattern: Pattern = Pattern.compile(
            "(([0-9]{1})([0-9]{0," + (digitsBeforeDecimal - 1) + "})?)?(\\.[0-9]{0," + digitsAfterDecimal + "})?"
    )

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val builder = StringBuilder(dest)
        builder.replace(dstart, dend, source.toString()
                .subSequence(start, end).toString())

        return if (!builder.toString().replace(",", "").matches((mPattern).toRegex())) {
            if (source.isEmpty()) dest.subSequence(dstart, dend) else ""
        } else null
    }
}

class DateFormatWatcher : TextWatcher {
    private var isFormatting: Boolean = false
    private var deletingHyphen: Boolean = false
    private var hyphenStart: Int = 0
    private var deletingBackward: Boolean = false

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return

        isFormatting = true

        s?.let {
            if (deletingHyphen && hyphenStart > 0) {
                if (deletingBackward) {
                    if (hyphenStart - 1 < it.length) {
                        it.delete(hyphenStart - 1, hyphenStart)
                    }
                } else if (hyphenStart < it.length) {
                    it.delete(hyphenStart, hyphenStart + 1)
                }
            }
            if (it.length == 2 || it.length == 5) {
                it.append('/')
            }
        }

        isFormatting = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (isFormatting) return

        val selStart = Selection.getSelectionStart(s)
        val selEnd = Selection.getSelectionEnd(s)
        s?.let {
            if (it.length > 1 && count == 1 && after == 0 && it[start] == '/' && selStart == selEnd) {
                deletingHyphen = true
                hyphenStart = start
                deletingBackward = selStart == start + 1
            } else {
                deletingHyphen = false
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}

fun isChar(c: String): Boolean {
    val regex = "[a-zA-Z .]*".toRegex()
    return c.matches(regex)
}

fun EditText.filterEditText(length: Int) {

    /* val regex1 = "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_ ]*".toRegex()
val regex2 = "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_ ]*".toRegex()
val regex = if (type == 1) regex1 else regex2

    val filter = InputFilter { source, start, end, _, _, _ ->
    for (i in start until end) {
        if (!source[i].toString().matches(regex)) return@InputFilter ""
    }
    null
} */

    val filter = InputFilter { source, start, end, _, _, _ ->
        var filtered = ""
        for (i in start until end) {
            val character = source[i]
            if (Character.isWhitespace(character) || Character.isLetter(character)) {
                filtered += character
            }
        }
        filtered
    }

    filters = arrayOf(filter, InputFilter.LengthFilter(length))
}

fun TextInputEditText.extractAmount(amount: String) {
    val inputText = amount.toCharArray()
    val amount: ArrayList<Char> = ArrayList()
    for (digit in inputText) {
        if (digit.isDigit() || digit == '-' || digit == '.') {
            amount.add(digit)
        }
    }
    setText(String(amount.toCharArray()))
}

fun toTitleCase(str: String?): String? {
    if (str == null) {
        return null
    }
    var space = true
    val builder = java.lang.StringBuilder(str)
    val len = builder.length
    for (i in 0 until len) {
        val c = builder[i]
        if (space) {
            if (!Character.isWhitespace(c)) {
                // Convert to title case and switch out of whitespace mode.
                builder.setCharAt(i, Character.toTitleCase(c))
                space = false
            }
        } else if (Character.isWhitespace(c)) {
            space = true
        } else {
            builder.setCharAt(i, Character.toLowerCase(c))
        }
    }
    return builder.toString()
}

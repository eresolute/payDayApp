package com.fh.payday.views2.shared.utils

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import io.reactivex.Observable

/**
 * Manages access to the Android soft keyboard.
 */
class KeyboardManager(private val activity: Activity) {

    /**
     * Observable of the status of the keyboard. Subscribing to this creates a
     * Global Layout Listener which is automatically removed when this
     * observable is disposed.
     */
    fun status(): Observable<KeyboardStatus> = Observable.create<KeyboardStatus> { emitter ->
        val rootView = activity.findViewById<View>(android.R.id.content)

        val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {

            val rect = Rect().apply { rootView.getWindowVisibleDisplayFrame(this) }

            val screenHeight = rootView.height

            // rect.bottom is the position above soft keypad or device button.
            // if keypad is shown, the rect.bottom is smaller than that before.
            val keypadHeight = screenHeight - rect.bottom

            // 0.15 ratio is perhaps enough to determine keypad height.
            if (keypadHeight > screenHeight * 0.15) {
                emitter.onNext(KeyboardStatus.OPEN)
            } else {
                emitter.onNext(KeyboardStatus.CLOSED)
            }
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        emitter.setCancellable {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }

    }.distinctUntilChanged()
}

enum class KeyboardStatus {
    OPEN, CLOSED
}
package com.hvdevs.playmedia.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object Utilities {
    //Esconder el teclado
    private fun hideSoftKeyboard(activity: Activity): Boolean {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(view: View, context: Context) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard(context as Activity)
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView, context)
            }
        }
    }

    private var isKeyboardShowing = false
    private fun onKeyboardVisibilityChanged(isOpen: Boolean) {
        print("keyboard $isOpen");
    }

    fun check(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect();
            view.getWindowVisibleDisplayFrame(r);
            val screenHeight = view.rootView.height;

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom;

            Log.d(TAG, "keypadHeight = $keypadHeight");

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true
                    onKeyboardVisibilityChanged(true)
                }
            } else {
                // keyboard is closed
                if (isKeyboardShowing) {
                    isKeyboardShowing = false
                    onKeyboardVisibilityChanged(false)
                }
            }
        }
    }
}
package com.hvdevs.playmedia.utilities

import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout

//Creamos una clase para en ella poner distintos tipos de funciones que controlen
//la adaptabilidad de la pantalla
object WindowSystemUtilities {

    //Con esta funcion logramos esconder la barra de estado y navegacion
    //Sacado de la pagina oficial de Android Studio
    fun hideSystemUI(window: Window) {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    //Mostramos la barra de estado y navegacion
    fun showSystemUI(window: Window) {
        val decorView: View = window.decorView
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
        decorView.systemUiVisibility = newUiOptions
    }

    //Chequeamos la orientacion, y le enviamos una variable tipo boolean para comprobar si esta o no
    //en landscape
    fun checkOrientation(newConfig: Configuration, window: Window, root: ConstraintLayout): Boolean {
        var boolean = false
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI(window) //Escondemos la UI del sistema
            root.fitsSystemWindows = false
            boolean = true
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            showSystemUI(window) //Mostramos la UI del sistema
            root.fitsSystemWindows = true
            boolean = false
        }
        return boolean
    }
}
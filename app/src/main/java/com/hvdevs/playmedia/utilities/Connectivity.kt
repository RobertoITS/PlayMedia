package com.hvdevs.playmedia.utilities

object Connectivity {
    fun isOnlineNet(): Boolean? {
        try {
            val p =
                Runtime.getRuntime().exec("ping -c 1 www.google.es")
            val `val` = p.waitFor()
            return `val` == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
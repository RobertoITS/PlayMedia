package com.hvdevs.playmedia.login.constructor

data class User(
    var active: Boolean = true,
    var email: String = "",
    var expire: String = "",
    var pass: String = "",
    var time: Long = 0L,
    var type: Int = 0,
    var uid: String = ""
)
package com.hvdevs.playmedia.constructor

import java.io.Serializable

data class User(
    var email: String = "",
    var expire: String = "",
    var pass: String = "",
    var time: Long = 0L,
    var type: Int = 0,
    var uid: String = ""
)
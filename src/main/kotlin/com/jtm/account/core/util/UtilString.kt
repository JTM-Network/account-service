package com.jtm.account.core.util

import java.lang.StringBuilder
import java.util.*

class UtilString {
    companion object {
        fun randomString(length: Int): String {
            return nextRandomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", Random())
        }

        private fun nextRandomString(length: Int, strAllowedCharacters: String, random: Random): String {
            val builder = StringBuilder(length)
            for (i in 0 until length) {
                val randomInt = random.nextInt(strAllowedCharacters.length)
                builder.append(strAllowedCharacters[randomInt])
            }
            return builder.toString()
        }
    }
}
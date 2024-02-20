package com.ruhlanusubov.byteninjax.extension

import android.util.Base64
import com.ruhlanusubov.byteninjax.util.Constants
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class PassDecoder(hash:String) {
    var kSpec:SecretKeySpec

    init {
        kSpec=SecretKeySpec(hash.toByteArray(),"AES")
    }

    fun decrypt(cipherText: String): String? {
        val bkdec = Base64.decode(cipherText.toByteArray(), 0)
        return try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(2, kSpec)
            String(cipher.doFinal(bkdec), charset("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}
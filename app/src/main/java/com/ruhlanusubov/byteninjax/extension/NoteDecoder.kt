package com.ruhlanusubov.byteninjax.extension

import android.app.Activity
import android.net.Uri
import android.nfc.Tag
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import com.ruhlanusubov.byteninjax.util.Constants
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NoteDecoder(var pass: String) {

    var kSpec:SecretKeySpec

    init {
        kSpec= SecretKeySpec(pass.md5().toString().toByteArray(),"AES")
    }
     fun String.md5(){
             try {
                val digest=MessageDigest.getInstance("MD5")
                digest.update(this.toByteArray())
                val messageDigest=digest.digest()
                val sb=StringBuffer()
                messageDigest.forEach {
                    val hex=Integer.toHexString(it.toInt() and 255)
                    if (hex.length==1){
                        sb.append("0")
                    }
                    sb.append(hex)
                }
                sb.toString()
            }catch (e:NoSuchAlgorithmException){
                Log.e("TAG", e.localizedMessage?:"" )
            }
        }

    fun getFile(activity:ComponentActivity):String?{
        val targetURI= Uri.parse(Constants.TARGET_URI)

        return try {
            activity.contentResolver.openInputStream(targetURI)?.use { input->
                input.bufferedReader().use {
                    val result=it.readText()
                    result.decrypt().toString()
                }
            }
        }catch (e:Exception){
            e.localizedMessage
            null
        }
    }

    fun String.decrypt(){
        val bkDec=Base64.decode(this.toByteArray(),0)
        try {
            val cipher= Cipher.getInstance("AES")
            cipher.init(2,kSpec)
            String(cipher.doFinal(this.toByteArray()), charset(Constants.UTF8))
        }catch (e:Exception){
            e.localizedMessage
            null
        }
    }



}
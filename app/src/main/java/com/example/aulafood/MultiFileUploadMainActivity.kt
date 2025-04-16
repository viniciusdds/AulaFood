package com.example.aulafood

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class MultiFileUploadMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiFileUploadScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiFileUploadScreen() {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var message by remember { mutableStateOf("") }

    // Launcher para selecionar múltiplos arquivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris = uris
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            filePickerLauncher.launch("application/pdf") // Aceita apenas PDFs
        }) {
            Text("Selecionar PDFs")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista os arquivos selecionados
        selectedUris.forEach { uri ->
            Text("Arquivo: ${getFileName3(uri, context)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedUris.isNotEmpty()) {
                    uploadFiles(selectedUris, context) { response ->
                        message = response
                    }
                } else {
                    message = "Nenhum arquivo selecionado!"
                }
            },
            enabled = selectedUris.isNotEmpty()
        ) {
            Text("Enviar PDFs")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

// Pega o nome do arquivo a partir da Uri
fun getFileName3(uri: Uri, context: android.content.Context): String {
    var name = "arquivo.pdf"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

// Envia múltiplos arquivos para a API
fun uploadFiles(uris: List<Uri>, context: android.content.Context, callback: (String) -> Unit) {
    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)

    // Adiciona cada arquivo ao corpo da requisição
    uris.forEach { uri ->
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(uri, context)
        val tempFile = File.createTempFile("upload_", ".pdf")
        FileOutputStream(tempFile).use { output ->
            inputStream?.copyTo(output)
        }
        requestBody.addFormDataPart(
            "pdfs[]", // Nome do campo (array no PHP)
            fileName,
            tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("https://clients.eadiaurora.com.br/MyCMS/clienteAG/api_teste.php") // Altere para sua URL
        .post(requestBody.build())
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "Resposta vazia"
            withContext(Dispatchers.Main) {
                callback(responseBody)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback("Erro: ${e.message}")
            }
        }
    }
}
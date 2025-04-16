package com.example.aulafood

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnvioPdfScreen()
        }
    }
}

@Composable
fun EnvioPdfScreen() {
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var mensagem by remember { mutableStateOf("") }
    val contexto = LocalContext.current

    val seletorPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pdfUri = uri
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = {
            seletorPdf.launch("application/pdf")
        }) {
            Text("Selecionar PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))

        pdfUri?.let { uri ->
            Text("Selecionado: ${getFileName2(uri, contexto)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (pdfUri != null) {
                val input = contexto.contentResolver.openInputStream(pdfUri!!)
                val nome = getFileName2(pdfUri!!, contexto)
                enviarPdf(input, nome) { resposta ->
                    mensagem = resposta
                }
            } else {
                mensagem = "Nenhum PDF selecionado"
            }
        }) {
            Text("Enviar PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(mensagem)
    }
}

fun getFileName2(uri: Uri, context: android.content.Context): String {
    var nome = "arquivo.pdf"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index != -1) {
            nome = cursor.getString(index)
        }
    }
    return nome
}

fun enviarPdf(inputStream: InputStream?, nomeArquivo: String, callback: (String) -> Unit) {
    if (inputStream == null) {
        callback("Erro: arquivo não encontrado")
        return
    }

    val tempFile = File.createTempFile("upload_", ".pdf")
    FileOutputStream(tempFile).use { output ->
        inputStream.copyTo(output)
    }

    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "arquivo", nomeArquivo,
            tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("https://clients.eadiaurora.com.br/MyCMS/clienteAG/api_teste.php") // Altere para sua URL
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val resposta = response.body?.string() ?: "Resposta vazia"
            withContext(Dispatchers.Main) {
                callback(resposta)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback("Erro: ${e.message}")
            }
        }
    }
}

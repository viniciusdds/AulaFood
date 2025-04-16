package com.example.aulafood

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*

class CadastroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnvioImagemScreen()
        }
    }
}

@Composable
fun EnvioImagemScreen() {
    var imagemUri by remember { mutableStateOf<Uri?>(null) }
    var imagemBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mensagem by remember { mutableStateOf("") }

    val contexto = LocalContext.current

    val seletorImagem = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imagemUri = uri
        imagemBitmap = null
    }

    val tiradorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imagemBitmap = bitmap
            imagemUri = null
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { seletorImagem.launch("image/*") }) {
                Text("Galeria")
            }

            Button(onClick = { tiradorFoto.launch(null) }) {
                Text("Câmera")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        imagemUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        imagemBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (imagemUri != null) {
                val input = contexto.contentResolver.openInputStream(imagemUri!!)
                val nomeArquivo = getFileName(imagemUri!!, contexto)
                enviarImagem(input, nomeArquivo) { resposta ->
                    mensagem = resposta
                }
            } else if (imagemBitmap != null) {
                val file = File.createTempFile("camera_", ".jpg", contexto.cacheDir)
                val stream = FileOutputStream(file)
                imagemBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.close()

                val input = FileInputStream(file)
                enviarImagem(input, file.name) { resposta ->
                    mensagem = resposta
                }
            } else {
                mensagem = "Nenhuma imagem selecionada"
            }
        }) {
            Text("Enviar Imagem")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(mensagem)
    }
}

fun getFileName(uri: Uri, context: android.content.Context): String {
    var nome = "imagem.jpg"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index != -1) {
            nome = cursor.getString(index)
        }
    }
    return nome
}

fun enviarImagem(inputStream: InputStream?, nomeArquivo: String, callback: (String) -> Unit) {
    if (inputStream == null) {
        callback("Erro: arquivo não encontrado")
        return
    }

    val tempFile = File.createTempFile("upload", nomeArquivo)
    FileOutputStream(tempFile).use { output ->
        inputStream.copyTo(output)
    }

    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "imagem", nomeArquivo,
            tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("https://clients.eadiaurora.com.br/MyCMS/clienteAG/api_teste.php") // Altere a URL se necessário
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: "Resposta vazia"
            withContext(Dispatchers.Main) {
                callback(responseText)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback("Erro: ${e.message}")
            }
        }
    }
}

package com.example.aulafood

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.json.JSONArray
import android.util.Log
import java.io.*

class MultipleImagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnvioImagensMultiplasScreen()
        }
    }
}

@Composable
fun EnvioImagensMultiplasScreen() {
    val contexto = LocalContext.current
    val scrollState = rememberScrollState()

    var imagemUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imagensCamera by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var mensagem by remember { mutableStateOf("") }

    val seletorImagens = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        imagemUris = uris
    }

    val tiradorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            imagensCamera = imagensCamera + it
        }
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxSize().verticalScroll(scrollState)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { seletorImagens.launch("image/*") }) {
                Text("Selecionar Imagens")
            }

            Button(onClick = { tiradorFoto.launch(null) }) {
                Text("Tirar Foto")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        imagemUris.forEach { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        imagensCamera.forEach { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val arquivosInput = mutableListOf<Pair<InputStream, String>>()

            imagemUris.forEach { uri ->
                val nome = getFileName4(uri, contexto)
                val input = contexto.contentResolver.openInputStream(uri)
                if (input != null) {
                    arquivosInput.add(Pair(input, nome))
                }
            }

            imagensCamera.forEachIndexed { index, bitmap ->
                val file = File.createTempFile("camera_$index", ".jpg", contexto.cacheDir)
                val output = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.close()
                arquivosInput.add(Pair(FileInputStream(file), file.name))
            }

            if (arquivosInput.isEmpty()) {
                mensagem = "Nenhuma imagem selecionada"
            } else {
                enviarMultiplasImagens(arquivosInput) { resposta ->
                    Log.d("UPLOAD_RESPOSTA", resposta)
                    mensagem = resposta
                }
            }
        }) {
            Text("Enviar Imagens")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(mensagem)
    }
}

fun getFileName4(uri: Uri, context: android.content.Context): String {
    var nome = "imagem.jpg"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index != -1) {
            nome = cursor.getString(index)
        }
    }
    return nome
}

fun enviarMultiplasImagens(
    arquivos: List<Pair<InputStream, String>>,
    callback: (String) -> Unit
) {
    val client = OkHttpClient()
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

    arquivos.forEachIndexed { index, (inputStream, nomeArquivo) ->
        val tempFile = File.createTempFile("upload_$index", null)
        FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }

        builder.addFormDataPart(
            "imagens[]", nomeArquivo,
            tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("https://clients.eadiaurora.com.br/MyCMS/clienteAG/api_teste.php")
        .post(builder.build())
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val resposta = response.body?.string() ?: "Resposta vazia"
            val mensagens = try {
                JSONArray(resposta).let { array ->
                    (0 until array.length()).joinToString("\n") { i -> array.getString(i) }
                }
            } catch (e: Exception) {
                resposta
            }

            withContext(Dispatchers.Main) {
                callback(mensagens)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback("Erro: ${e.message}")
            }
        }
    }
}


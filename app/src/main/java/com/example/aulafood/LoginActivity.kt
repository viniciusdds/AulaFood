package com.example.aulafood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    // Estados para guardar email, senha e resultado da API
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    // Layout da tela
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Quando clicar, chama a função de login
                fazerLogin(email, senha) { resposta ->
                    mensagem = resposta
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(mensagem)
    }
}

// Função que faz a chamada POST para a API em PHP
fun fazerLogin(email: String, senha: String, callback: (String) -> Unit) {
    val client = OkHttpClient()

    // Cria o JSON manualmente
    val json = """
        {
            "email": "$email",
            "senha": "$senha"
        }
    """.trimIndent()

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    // Altere a URL para o endereço da sua API PHP
    val request = Request.Builder()
        .url("https://clients.eadiaurora.com.br/MyCMS/clienteAG/api_teste.php") // 10.0.2.2 = localhost no emulador Android
        .post(body)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val respostaJson = response.body?.string() ?: "Erro na resposta"

            // Atualiza a UI na thread principal
            withContext(Dispatchers.Main) {
                callback(respostaJson)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback("Erro: ${e.message}")
            }
        }
    }
}

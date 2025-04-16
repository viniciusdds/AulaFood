package com.example.aulafood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aulafood.ui.theme.AulaFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AulaFoodTheme {
                LoginFood()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFood() {

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImageLogo(R.drawable.img_verduras)
            BoasVindas()
            UsernameTextField(user) { user = it }
            PasswordTextField(pass, passwordVisible) { pass = it }
            LoginButton()
            ForgotPasswordText()
            SocialMediaIcons()
        }
    }

}

@Composable
fun SocialMediaIcons() {
   val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        SocialMediaIcon(R.drawable.google){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            context.startActivity(intent)
        }
        SocialMediaIcon(R.drawable.twitter){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com"))
            context.startActivity(intent)
        }
        SocialMediaIcon(R.drawable.facebook){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"))
            context.startActivity(intent)
        }
    }
}

@Composable
fun SocialMediaIcon(resId: Int, onClick: () -> Unit){
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    )
}

@Composable
fun ForgotPasswordText() {
    Text(
        text = "Não lembra a senha? clique aqui",
        modifier = Modifier
            .padding(vertical = 8.dp),
        fontSize = 14.sp,
        color = Color(0xFF4CAF50),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LoginButton() {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 54.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = "Login",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PasswordTextField(value: String, passwordVisibility: Boolean, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        visualTransformation = if (passwordVisibility) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = if (passwordVisibility) {
            KeyboardOptions.Default
        } else {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        },
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 54.dp, vertical = 5.dp),
        shape = RoundedCornerShape(50),
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            color = Color(0xFF4CAF50),
            fontSize = 14.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7D32A8),
            unfocusedBorderColor = Color(0xFF7D32A8),
            focusedContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = "Senha",
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun UsernameTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 54.dp, vertical = 5.dp),
        shape = RoundedCornerShape(50),
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            color = Color(0xFF4CAF50),
            fontSize = 14.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7D32A8),
            unfocusedBorderColor = Color(0xFF7D32A8),
            focusedContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = "Usuário",
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun BoasVindas() {
    Text(
        text = "Bem Vindo ao Canal \n" + "Android Studio Simples \n" + "e Direto",
        fontSize = 30.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4CAF50),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ImageLogo(resId: Int) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = Modifier.height(350.dp)
    )
}


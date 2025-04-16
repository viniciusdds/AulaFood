package com.example.aulafood

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.aulafood.ui.theme.AulaFoodTheme
import kotlinx.coroutines.delay

class IntoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AulaFoodTheme {
                TelaSplash(this)
            }
        }
    }
}

@Composable
fun TelaSplash(activity: ComponentActivity) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(Unit) {
        delay(timeMillis = 3000L)
        navigateToMainActivity(activity)
    }
}

fun navigateToMainActivity(activity: ComponentActivity) {
    val intent = Intent(activity, MainActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
}

//@Preview(showBackground = true)
//@Composable
//fun TelaSplashPreview(){
//    AulaFoodTheme {
//        TelaSplash(ComponentActivity())
//    }
//}
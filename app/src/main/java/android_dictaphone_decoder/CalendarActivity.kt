package android_dictaphone_decoder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.CalendarView
import android_dictaphone_decoder.theme.AppColors
import android_dictaphone_decoder.theme.SpeechToTextTheme
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.android_dictaphone_decoder.R

class CalendarActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeechToTextTheme {
                Calendar(this)
            }
        }
    }
}

@Composable
fun Calendar(activity: ComponentActivity) {
    var date by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.gradientBottom,
                        AppColors.gradientMid,
                        AppColors.gradientTop
                    )
                )
            )
    ) {
        AndroidView(
            modifier = Modifier
                .background(color = AppColors.Ghost, shape = RoundedCornerShape(16.dp)),
            factory = { context ->
                CalendarView(ContextThemeWrapper(context, R.style.CalenderViewCustom))
            },
            update = {
                it.solidColor
                it.setOnDateChangeListener { _, year, month, day ->
                    date = "$day - ${month + 1} - $year"
                    try {
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.putExtra("selected_date", date)
                        activity.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println(e)
                    }
                }
            }
        )
    }
}

package com.example.newspapertemplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newspapertemplate.compose.ArticleTemplate
import com.example.newspapertemplate.compose.ArticleText
import com.example.newspapertemplate.compose.CircleText

val STYLE = TextStyle(
    fontSize = 12.sp
)

val TITLE_STYLE = TextStyle(
    fontSize = 32.sp
)

@Composable
fun sushiImage() {
    val image = painterResource(id = R.drawable.sushi)
    val width = 180.dp
    val imageSize = image.intrinsicSize
    val height = width * imageSize.height / imageSize.width
    Image(
        image,
        "image",
        modifier = Modifier
            .padding(5.dp)
            .requiredWidth(width)
            .requiredHeight(height) // why not automatically done?

    )
}

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Left Figure", style = TITLE_STYLE, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    ArticleText(text = stringResource(id = R.string.lorem_ipsum), textStyle = STYLE, template = ArticleTemplate.Figure_Left) {
                        sushiImage()
                    }

                    Text(text = "Center Figure", style = TITLE_STYLE, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    ArticleText(text = stringResource(id = R.string.lorem_ipsum), textStyle = STYLE, template = ArticleTemplate.Figure_Center) {
                        sushiImage()
                    }

                    Text(text = "Right Figure", style = TITLE_STYLE, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    ArticleText(text = stringResource(id = R.string.lorem_ipsum), textStyle = STYLE, template = ArticleTemplate.Figure_Right) {
                        sushiImage()
                    }

                    Text(text = "Circle In", style = TITLE_STYLE, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    CircleText(text = stringResource(id = R.string.lorem_ipsum), textStyle = STYLE)

                }

        }
    }
}
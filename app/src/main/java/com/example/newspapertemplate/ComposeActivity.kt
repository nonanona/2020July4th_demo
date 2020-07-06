package com.example.newspapertemplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.Composable
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.TextStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.example.newspapertemplate.compose.ArticleTemplate
import com.example.newspapertemplate.compose.ArticleText
import com.example.newspapertemplate.compose.CircleText

val STYLE = TextStyle(
    fontSize = 16.sp
)

val TITLE_STYLE = TextStyle(
    fontSize = 32.sp
)

@Composable
fun sushiImage() {
    val image = imageResource(id = R.drawable.sushi)
    val width = 180.dp
    val height = width * image.height / image.width
    Image(
        image,
        modifier = Modifier
            .padding(5.dp)
            .preferredWidth(width)
            .preferredHeight(height) // why not automatically done?

    )
}

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VerticalScroller() {
                Column {
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
}
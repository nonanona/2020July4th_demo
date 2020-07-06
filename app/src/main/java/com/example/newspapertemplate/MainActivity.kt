package com.example.newspapertemplate

import android.graphics.Canvas
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.newspapertemplate.text.Line
import com.example.newspapertemplate.text.NextPositionCallback
import com.example.newspapertemplate.text.WidthNegotiationCallback
import com.example.newspapertemplate.views.Article
import com.example.newspapertemplate.views.ArticleTextView

data class Demo(
    val title: String,
    val nextPosition: NextPositionCallback,
    val widthNegotiation: WidthNegotiationCallback,
    val boundsDrawer: (Canvas) -> Unit
)

private class DemoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val descriptionView = view.findViewById<TextView>(R.id.description)
    private val article = view.findViewById<ArticleTextView>(R.id.article)

    fun bindDemo(demo: Demo) {
        descriptionView.text = demo.title
        article.demo = demo
    }
}

private class DemoListAdapter(
    private val demos: List<Demo>
) : RecyclerView.Adapter<DemoListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoListViewHolder {
        try {
            return DemoListViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.demo_container, parent, false
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("Debug", "Error", e)
            throw e
        }
    }

    override fun getItemCount(): Int = demos.count()

    override fun onBindViewHolder(holder: DemoListViewHolder, position: Int) {
        val item = demos[position]
        holder.bindDemo(item)
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ViewPager2>(R.id.viewPager).apply {
            adapter = DemoListAdapter(DEMOS)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    title = "${position + 1}/${DEMOS.size}"
                }
            })
        }
    }
}
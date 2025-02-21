package io.kodular.technologygyan957.Tag_Finder_and_Thumbnail_Downloader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    private val API_KEY = "Enter  your api i had removed beofre publishing on gihtub"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val inputUrl: EditText = findViewById(R.id.inputUrl)
        val buttonFindTags: Button = findViewById(R.id.buttonFindTags)
        val textTags: TextView = findViewById(R.id.textTags)
        val buttonCopyTags: Button = findViewById(R.id.buttonCopyTags)

        buttonFindTags.setOnClickListener {
            val url = inputUrl.text.toString().trim()
            val videoId = extractVideoId(url)

            if (videoId != null) {
                fetchTags(videoId, textTags)
            } else {
                textTags.text = "❌ Invalid YouTube URL"
            }
        }

        buttonCopyTags.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("YouTube Tags", textTags.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Tags Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractVideoId(url: String): String? {
        val regex = Regex("(?<=v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v=|/shorts/|watch\\?feature=player_embedded&v=|&v=)([^#&?]*).*")
        val matchResult = regex.find(url)
        return matchResult?.groups?.get(1)?.value
    }

    private fun fetchTags(videoId: String, textTags: TextView) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$videoId&key=$API_KEY"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val items = response.getJSONArray("items")
                if (items.length() > 0) {
                    val snippet = items.getJSONObject(0).getJSONObject("snippet")
                    val tagsArray = snippet.optJSONArray("tags")

                    if (tagsArray != null) {
                        val tagsList = (0 until tagsArray.length()).map { tagsArray.getString(it) }
                        textTags.text = tagsList.joinToString(" #", "#")
                    } else {
                        textTags.text = "No tags found for this video."
                    }
                } else {
                    textTags.text = "Invalid Video ID."
                }
            },
            { error ->
                textTags.text = "Error fetching tags: ${error.message}"
            })

        queue.add(jsonObjectRequest)
    }
}

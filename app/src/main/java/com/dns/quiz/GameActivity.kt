package com.dns.quiz

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dns.quiz.data.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody


class GameActivity : AppCompatActivity() {
    private var index: Int = 0
    private var questions: List<Question> = emptyList()
    private val answers = mutableListOf<String>()
    private val ids: HashMap<String, Int> = hashMapOf(
        "title" to R.id.title, "name" to R.id.name, "loading" to R.id.loading, "button" to R.id.proceed,
        "option1" to R.id.option1, "option2" to R.id.option2, "option3" to R.id.option3,
        "option4" to R.id.option4, "option5" to R.id.option5
    )

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // DO NOTHING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        controlVisibility(true)
        runApp(OkHttpClient(), "https://viacep.com.br/ws/01001000/json/")
    }

    private fun runApp(client: OkHttpClient, quizURL: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val content = getContent(client, quizURL)
            content?.string()?.let { responseBody ->
                Log.d("GameActivity", responseBody)
                this@GameActivity.questions = Json.decodeFromString(getMock())
                withContext(Dispatchers.Main) {
                    this@GameActivity.controlVisibility(false)
                    this@GameActivity.controlContent()
                }
            }
        }
    }

    private fun getContent(client: OkHttpClient, url: String): ResponseBody? {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body
    }

    private fun controlVisibility(loading: Boolean) {
        for (key in this.ids.keys) {
            val show: Boolean = (key == "loading" && loading) || (key != "loading" && !loading)
            val content: TextView = findViewById(this.ids[key]!!)
            content.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun controlContent() {
        val question: Question = this.questions[this.index]
        val isLast: Boolean = this.index + 1 == this.questions.size

        val title: TextView = findViewById(this.ids["title"]!!)
        title.text = question.title

        val button: Button = findViewById(this.ids["button"]!!)
        button.text = if (isLast) "Finish" else "Proceed"

        button.setOnClickListener {
          if (isLast) handleFinish()
          else handleProceed()
        }

        this.controlOptions(question)
    }

    private fun controlOptions(question: Question) {
        val default = this.getDefaultColor()
        for (key in this.ids.keys) {
            if (!key.contains("option")) continue
            Log.d("GameActivity", this.answers.toString())
            val content: Button = findViewById(this.ids[key]!!)
            val option = question.options[key.removePrefix("option").toInt()-1]
            val selected = (this.index+1 <= this.answers.size) && this.answers[this.index] == option.key
            val color = if (selected) Color.parseColor("#2196F3") else default

            content.setBackgroundColor(color)
            content.setOnClickListener {
                if (this.index+1 > this.answers.size) this.answers.add(option.key)
                else this.answers[this.index] = option.key
                this.controlOptions(question)
            }

        }
    }

    private fun handleFinish() {
        Log.d("GameActivity", "Finish")
    }

    private fun handleProceed() {
        this.index += 1
        this.controlContent()
    }

    private fun getDefaultColor(): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        return typedValue.data
    }

    private fun getMock(): String {
        return """
            [
              {
                "id": "1",
                "title": "Question 1",
                "options": [
                  {"key": "zsh", "value": "option 1"},
                  {"key": "zzz", "value": "option 2"},
                  {"key": "aaa", "value": "option 3"},
                  {"key": "bbb", "value": "option 4"},
                  {"key": "ccc", "value": "option 5"}
                ],
                "answer": "zsh"
              },
              {
                "id": "2",
                "title": "Question 2",
                "options": [
                  {"key": "zsh", "value": "option 1"},
                  {"key": "zzz", "value": "option 2"},
                  {"key": "aaa", "value": "option 3"},
                  {"key": "bbb", "value": "option 4"},
                  {"key": "ccc", "value": "option 5"}
                ],
                "answer": "bbb"
              }
            ]
        """
    }

}
package com.dns.quiz

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dns.quiz.data.Answer
import com.dns.quiz.data.Question
import com.dns.quiz.data.Theme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody


class GameActivity : AppCompatActivity() {
    private var index: Int = 0
    private var questions: List<Question> = emptyList()
    private val answers = mutableListOf<Answer>()
    private val ids: HashMap<String, Int> = hashMapOf(
        "title" to R.id.title, "name" to R.id.name, "loading" to R.id.loading, "button" to R.id.proceed,
        "option1" to R.id.option1, "option2" to R.id.option2, "option3" to R.id.option3,
        "option4" to R.id.option4, "option5" to R.id.option5, "name" to R.id.name
    )

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // DO NOTHING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        controlVisibility(true)
        runApp(OkHttpClient(), "https://ad20-179-251-80-128.ngrok-free.app/quiz")
    }

    private fun runApp(client: OkHttpClient, quizURL: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val content = getContent(client, quizURL)
            content?.string()?.let { responseBody ->
                Log.d("GameActivity", responseBody)
                this@GameActivity.questions = Json.decodeFromString(responseBody)
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

        findViewById<TextView>(this.ids["title"]!!).text = "Question ${this.index + 1}"
        findViewById<TextView>(this.ids["name"]!!).text = question.name

        val button: Button = findViewById(this.ids["button"]!!)
        button.text = if (isLast) "Finish" else "Proceed"

        button.setOnClickListener {
          if (isLast) handleFinish()
          else handleProceed()
        }

        this.controlOptions(question)
    }

    private fun controlOptions(question: Question) {
        val theme = this.handleGetTheme()
        for (key in this.ids.keys) {
            if (!key.contains("option")) continue
            val content: Button = findViewById(this.ids[key]!!)
            val idx = key.removePrefix("option").toInt()-1
            val option = question.options[idx]
            val selected = (this.index+1 <= this.answers.size) && this.answers[this.index].value == option.key

            content.setBackgroundColor(if (selected) Color.parseColor("#2196F3") else theme.primary)
            content.setTextColor(if (selected) theme.primary else theme.text)
            content.text = question.options[idx].value

            content.setOnClickListener {
                val currentAnswer = Answer(key, option.key)
                if (this.index+1 > this.answers.size) this.answers.add(currentAnswer)
                else this.answers[this.index] = currentAnswer
                this.controlOptions(question)
            }
        }
    }

    private fun handleFinish() {
        this.checkAnswer()
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("questions", Gson().toJson(this.questions))
        intent.putExtra("answers", Gson().toJson(this.answers))

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                startActivity(intent)
            }
        }
    }

    private fun handleProceed() {
        this.checkAnswer()
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                this@GameActivity.index += 1
                this@GameActivity.controlContent()
            }
        }
    }

    private fun checkAnswer() {
        val question: Question = this.questions[this.index]
        val answer = this.answers[this.index]
        val content: Button = findViewById(this.ids[answer.id]!!)
        val color =  if (question.answer != answer.value) "#F43C53" else "#95CD97"
        content.setBackgroundColor(Color.parseColor(color))
    }

    private fun handleGetTheme(): Theme {
        val button = TypedValue()
        val text = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, button, true)
        theme.resolveAttribute(android.R.attr.colorAccent, text, true)
        return Theme(button.data, text.data)
    }
}
package com.dns.quiz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dns.quiz.data.Answer
import com.dns.quiz.data.Question
import kotlinx.serialization.json.Json

class ResultActivity : AppCompatActivity() {
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // DO NOTHING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        findViewById<Button>(R.id.proceed).setOnClickListener { handleReset() }

        val questionsJson = intent.getStringExtra("questions")
        val answersJson = intent.getStringExtra("answers")

        val questions: List<Question> = Json.decodeFromString(questionsJson!!)
        val answers: List<Answer> = Json.decodeFromString(answersJson!!)

        val result = this.getResult(questions, answers)

        findViewById<TextView>(R.id.info).text = "You got $result out of ${questions.size} questions"
        findViewById<ImageView>(R.id.img).setImageResource(this.getImage(questions.size, result))
    }

    private fun getResult(questions: List<Question>, answers: List<Answer>): Int {
        var correct = 0
        for (index in questions.indices) {
            if (questions[index].answer == answers[index].value) correct++
        }
        return correct
    }

    private fun handleReset() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun getImage(size: Int, result: Int): Int {
        val middle = size/2
        if (result < middle) return R.drawable.skull
        else if (result == middle) return R.drawable.neutral
        return R.drawable.star
    }
}
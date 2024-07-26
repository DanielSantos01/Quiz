package com.dns.quiz.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val name: String,
    val answer: String,
    val options: List<Option>
)
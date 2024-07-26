package com.dns.quiz.data

import kotlinx.serialization.Serializable

@Serializable
data class Answer(val id: String, val value: String)
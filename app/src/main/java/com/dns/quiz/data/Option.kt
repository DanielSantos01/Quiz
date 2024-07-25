package com.dns.quiz.data

import kotlinx.serialization.Serializable

@Serializable
data class Option(val key: String, val value: String)
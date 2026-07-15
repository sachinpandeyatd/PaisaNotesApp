package com.paisanotes.domain.model

data class Person(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val totalExposure: Double = 0.0
)
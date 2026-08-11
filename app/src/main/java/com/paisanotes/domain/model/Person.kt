package com.paisanotes.domain.model

data class Person(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val loanExposure: Double = 0.0,
    val emiExposure: Double = 0.0
){
    val combinedExposure: Double get() = loanExposure + emiExposure
}
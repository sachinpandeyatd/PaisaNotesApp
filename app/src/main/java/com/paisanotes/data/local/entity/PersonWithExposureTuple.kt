package com.paisanotes.data.local.entity

import androidx.room.Embedded

data class PersonWithExposureTuple(
    @Embedded val person: PersonEntity,
    val totalExposure: Double
)
package com.intec.telemedicina.model

import com.intec.telemedicina.repositories.dto.Location
import com.intec.telemedicina.repositories.dto.Origin

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: Origin,
    val location: Location,
    val image: String
)

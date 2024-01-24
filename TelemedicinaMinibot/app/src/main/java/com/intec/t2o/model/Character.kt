package com.intec.t2o.model

import com.intec.t2o.repositories.dto.Location
import com.intec.t2o.repositories.dto.Origin

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

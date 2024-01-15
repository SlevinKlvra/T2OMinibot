package com.intec.telemedicina.data

data class MeetingResponse(
    val id: Int,
    val start_date: String,
    val end_date: String,
    val start_time: String,
    val end_time: String,
    val nombre: String,
    val apellidos: String,
    val puntomapa: String,
)

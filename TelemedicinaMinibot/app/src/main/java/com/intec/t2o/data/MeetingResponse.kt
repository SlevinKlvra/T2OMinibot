package com.intec.t2o.data

data class MeetingResponse(
    val usuario : String,
    val id: Int,
    val start_date: String,
    val end_date: String,
    val start_time: String,
    val end_time: String,
    val anfitrion: String,
    val email_anfitrion : String,
    val puntomapa: String,
    val visitante: String,
    val email_visitante: String,
)


package com.intec.telemedicina.api

import com.intec.telemedicina.repositories.dto.CharacterDto
import com.intec.telemedicina.repositories.dto.CharactersDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character/")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): CharactersDto



    @GET("character/{id}")
    suspend fun  getCharacter(
        @Path("id") id: Int
    ): CharacterDto
}
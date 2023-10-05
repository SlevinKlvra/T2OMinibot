package com.intec.telemedicina.model

import kotlinx.coroutines.flow.Flow
import com.intec.telemedicina.data.*
interface CharacterRepository {
    fun getCharacters(page: Int): Flow<Result<List<Characters>>>
    suspend fun getCharacter(id: Int): Result<Character>
}
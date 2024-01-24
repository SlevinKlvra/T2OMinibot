package com.intec.t2o.model

import kotlinx.coroutines.flow.Flow
import com.intec.t2o.data.*
interface CharacterRepository {
    fun getCharacters(page: Int): Flow<Result<List<Characters>>>
    suspend fun getCharacter(id: Int): Result<Character>
}
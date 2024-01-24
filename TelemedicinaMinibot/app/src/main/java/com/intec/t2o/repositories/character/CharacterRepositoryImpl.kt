package com.intec.t2o.repositories.character

import com.intec.t2o.api.RickAndMortyApi
import com.intec.t2o.model.CharacterRepository
import com.intec.t2o.model.Character
import com.intec.t2o.model.Characters
import com.intec.t2o.repositories.dto.toCharacter
import com.intec.t2o.repositories.dto.toListCharacters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.intec.t2o.data.*

class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi
): CharacterRepository {
    //@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun getCharacters(page: Int): Flow<Result<List<Characters>>> = flow{
        emit(Result.Loading())
        try {
            val response = api.getCharacters(page).toListCharacters()
            emit(Result.Success(response))

        }
        catch (e: HttpException){
            emit(
                Result.Error(
                    message = "ops, algo salio mal",
                    data = null
                ))
        } catch (e: IOException){
            emit(
                Result.Error(
                    message = "no se puede encontrar el servidor",
                    data = null
                ))
        }
    }

    override suspend fun getCharacter(id: Int): Result<Character> {
        val response = try {
            api.getCharacter(id)

        }catch (e: Exception){
            return Result.Error("Error",null)
        }
        return Result.Success(response.toCharacter())
    }
}
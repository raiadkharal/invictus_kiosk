package net.invictusmanagement.invictuskiosk.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.LoginDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toLogin
import net.invictusmanagement.invictuskiosk.domain.model.Login
import net.invictusmanagement.invictuskiosk.domain.repository.LoginRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
) : LoginRepository {
    override fun login(loginDto: LoginDto): Flow<Resource<Login>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.login(loginDto).toLogin()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("login","Error logging in: ${e.localizedMessage}", e)
            if (e.code() == 400) {
                emit(Resource.Error("The specified code is invalid. Please check the code and try again."))
            } else {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}
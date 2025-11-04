package net.invictusmanagement.invictuskiosk.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

inline fun <T> safeApiFlow(
    crossinline apiCall: suspend () -> T
): Flow<Resource<T>> = flow {
    emit(Resource.Loading())
    try {
        val result = apiCall()
        emit(Resource.Success(result))
    } catch (e: Exception) {
        when (e) {
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException -> emit(Resource.Error("No Internet Connection."))
            is HttpException -> emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            is IOException -> emit(Resource.Error("Couldn't reach server."))
            else -> emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}

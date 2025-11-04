package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.MobileApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val api: MobileApiInterface,
) : LogRepository {

    override fun postKioskLog(log: RelayManagerLogDto): Flow<Resource<RelayManagerLogDto>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.postLog(log)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}

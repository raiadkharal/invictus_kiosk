package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.MissedCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.toMissedCall
import net.invictusmanagement.invictuskiosk.data.remote.dto.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.data.remote.dto.toVideoCall
import net.invictusmanagement.invictuskiosk.data.remote.dto.toVideoCallToken
import net.invictusmanagement.invictuskiosk.domain.model.MissedCall
import net.invictusmanagement.invictuskiosk.domain.model.VideoCall
import net.invictusmanagement.invictuskiosk.domain.model.VideoCallToken
import net.invictusmanagement.invictuskiosk.domain.repository.VideoCallRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VideoCallRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
):VideoCallRepository {
    override fun getVideoCallToken(room: String): Flow<Resource<VideoCallToken>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getVideoCallToken(room).toVideoCallToken()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("getvideocalltoken", "Error fetching video call token ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun connectToVideoCall(videoCallDto: VideoCallDto): Flow<Resource<VideoCall>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.connectToVideoCall(videoCallDto).toVideoCall()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("connectToVideoCall", "Error connecting to video call ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun postMissedCall(missedCallDto: MissedCallDto): Flow<Resource<MissedCall>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.postMissedCall(missedCallDto).toMissedCall()
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("postMissedCall", "Error posting missed call ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}
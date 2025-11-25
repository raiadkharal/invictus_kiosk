package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.commons.safeApiCall
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
    private val logTag = "VideoCallRepository"

    override fun getVideoCallToken(room: String): Flow<Resource<VideoCallToken>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-getVideoCallToken",
                remoteCall = { api.getVideoCallToken(room).toVideoCallToken() },
                errorMessage = "Failed to fetch video call token"
            )
        )
    }

    override fun connectToVideoCall(videoCallDto: VideoCallDto): Flow<Resource<VideoCall>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-connectToVideoCall",
                remoteCall = { api.connectToVideoCall(videoCallDto).toVideoCall() },
                errorMessage = "Failed to connect to video call"
            )
        )
    }

    override fun postMissedCall(missedCallDto: MissedCallDto): Flow<Resource<MissedCall>> = flow {
        emit(Resource.Loading())

        emit(
            safeApiCall(
                logger = logger,
                tag = "$logTag-postMissedCall",
                remoteCall = { api.postMissedCall(missedCallDto).toMissedCall() },
                errorMessage = "Failed to post missed call"
            )
        )
    }
}
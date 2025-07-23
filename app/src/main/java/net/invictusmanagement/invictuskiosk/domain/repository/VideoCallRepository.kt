package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.MissedCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallDto
import net.invictusmanagement.invictuskiosk.domain.model.MissedCall
import net.invictusmanagement.invictuskiosk.domain.model.VideoCall
import net.invictusmanagement.invictuskiosk.domain.model.VideoCallToken

interface VideoCallRepository {
    fun getVideoCallToken(room: String): Flow<Resource<VideoCallToken>>
    fun connectToVideoCall(videoCallDto: VideoCallDto): Flow<Resource<VideoCall>>
    fun postMissedCall(missedCallDto: MissedCallDto): Flow<Resource<MissedCall>>

}
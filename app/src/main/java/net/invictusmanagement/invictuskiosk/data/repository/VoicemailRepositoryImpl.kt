package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.ApiInterface
import net.invictusmanagement.invictuskiosk.data.remote.dto.toPromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.VoicemailRepository
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class VoicemailRepositoryImpl @Inject constructor(
    private val api: ApiInterface,
    private val logger: GlobalLogger
): VoicemailRepository {
    override fun uploadVoicemail(file: File, userId: Long): Flow<Resource<Long>> = flow{
        try {
            emit(Resource.Loading())
            val videoPart = MultipartBody.Part.createFormData(
                "VideoFile", file.name, file.asRequestBody("video/webm".toMediaTypeOrNull())
            )
            val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadVoicemail(videoPart,userIdPart)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            logger.logError("uploadVoicemail", "Error uploading voicemail: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("${e.localizedMessage} Couldn't reach server. Check your internet connection."))
        }
    }

}
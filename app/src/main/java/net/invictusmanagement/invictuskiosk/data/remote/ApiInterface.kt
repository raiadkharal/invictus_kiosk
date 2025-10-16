package net.invictusmanagement.invictuskiosk.data.remote

import net.invictusmanagement.invictuskiosk.data.remote.dto.AccessPointDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.BusinessPromotionDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.ContactRequestDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.LeasingOfficeDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.LoginDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.MissedCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.PromotionsCategoryDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.ResidentDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.ServiceKeyDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.UnitDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.UnitListDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.VideoCallTokenDto
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.MainDto
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("accountkiosk/login")
    suspend fun login(@Body loginDto: LoginDto): LoginDto

    @POST("digitalkey/user")
    suspend fun validateDigitalKey(@Body digitalKeyDto: DigitalKeyDto): DigitalKeyDto

    @POST("digitalkey/location")
    suspend fun validateServiceKey(@Body serviceKeyDto: ServiceKeyDto): ServiceKeyDto

    @GET("accesspoints")
    suspend fun getAccessPoints(): List<AccessPointDto>

    @GET("units")
    suspend fun getUnits(): List<UnitDto>

    @GET("units/listofunits")
    suspend fun getUnitList(): List<UnitListDto>

    @GET("home/kiosk")
    suspend fun getKioskData(): MainDto

    @GET("residents/{filter}/{byName}")
    suspend fun getResidentsByName(
        @Path("filter") filter: String,
        @Path("byName") byName: String
    ): List<ResidentDto>

    @GET("residents/getbyunit/{unitNumber}")
    suspend fun getResidentsByUnitNumber(
        @Path("unitNumber") unitNumber: String
    ): List<ResidentDto>

    @GET("residents/!/f")
    suspend fun getAllResidents(): List<ResidentDto>

    @GET("residents/*/{byName}")
    suspend fun getAllLeasingAgents(
        @Path("byName") byName: String
    ): List<ResidentDto>

    @GET("accesspoints/getleasingofficedetails")
    suspend fun getLeasingOfficeDetails(): LeasingOfficeDto

    @GET("promotions/categories")
    suspend fun getPromotionCategories(): List<PromotionsCategoryDto>

    @GET("promotions/filter/{id}")
    suspend fun getPromotionsByCategory(
        @Path("id") id: String
    ): List<BusinessPromotionDto>

    @POST("units")
    suspend fun sendContactRequest(@Body contactRequest: ContactRequest): ContactRequestDto

    @GET("chat/token")
    suspend fun getVideoCallToken(
        @Query("room") room: String
    ): VideoCallTokenDto

    @POST("chat/call")
    suspend fun connectToVideoCall(@Body videoCallDto: VideoCallDto): VideoCallDto

    @POST("chat/missed")
    suspend fun postMissedCall(@Body missedCallDto: MissedCallDto): MissedCallDto

    @GET("accesspoints/getintrobuttons")
    suspend fun getIntroButtons(): List<String>

    @Multipart
    @POST("videomail/savevideomail")
    suspend fun uploadVoicemail(
        @Part videoFile: MultipartBody.Part,
        @Part("UserId") userId: RequestBody
    ): Long

    @GET("units/{unitId}/maps/{unitMapId}")
    suspend fun getMapImage(
        @Path("unitId") unitId: Long,
        @Path("unitMapId") unitMapId: Long,
        @Query("toPackageCenter") toPackageCenter: Boolean = false
    ): ResponseBody
}
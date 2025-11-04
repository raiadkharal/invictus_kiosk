package net.invictusmanagement.invictuskiosk.data.remote

import net.invictusmanagement.invictuskiosk.data.remote.dto.RelayManagerLogDto
import retrofit2.http.Body
import retrofit2.http.POST

interface MobileApiInterface {

    @POST("relaymanager/log")
    suspend fun postLog(@Body relayManagerLogDto: RelayManagerLogDto): RelayManagerLogDto
}
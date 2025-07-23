package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.commons.Resource
import java.io.File

interface VoicemailRepository {
    fun uploadVoicemail(file: File, userId: Long): Flow<Resource<Long>>
}
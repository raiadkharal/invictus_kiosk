package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.invictusmanagement.invictuskiosk.data.local.entities.LogType
import net.invictusmanagement.invictuskiosk.data.local.entities.SystemLogEntity

@Dao
interface SystemLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLogEntity)

    @Query("SELECT * FROM system_logs WHERE logType = :type ORDER BY createdAt DESC")
    fun getLogsByType(type: LogType): List<SystemLogEntity>

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM system_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

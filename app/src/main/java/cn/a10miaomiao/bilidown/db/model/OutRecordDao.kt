package cn.a10miaomiao.bilidown.db.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cn.a10miaomiao.bilidown.db.dao.OutRecord

@Dao
interface OutRecordDao {
    @Query("SELECT * FROM out_record order by update_time desc")
    suspend fun getAll(): List<OutRecord>

    @Query("SELECT * FROM out_record WHERE input_path IN (:entryDirPaths)")
    suspend fun loadAllByEntryDirPaths(entryDirPaths: Array<String>): List<OutRecord>

    @Query("SELECT * FROM out_record WHERE input_path=:path")
    suspend fun findByPath(path: String): OutRecord?

    @Insert
    suspend fun insertAll(vararg task: OutRecord)

    @Delete
    suspend fun delete(user: OutRecord)
}
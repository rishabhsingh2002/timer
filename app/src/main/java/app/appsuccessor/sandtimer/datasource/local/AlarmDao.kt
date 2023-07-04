package app.appsuccessor.sandtimer.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.appsuccessor.sandtimer.model.Alarm

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): LiveData<List<Alarm>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlarm(alarm: Alarm)

    @Update
    fun updateAlarm(alarm: Alarm)

    @Query("DELETE FROM alarms WHERE id=:id")
    fun deleteAlarm(id: Int)
}

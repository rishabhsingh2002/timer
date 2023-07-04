package app.appsuccessor.sandtimer.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.appsuccessor.sandtimer.model.Alarm
import app.appsuccessor.sandtimer.model.ClockCity


@Dao
interface ClockDao {
    @Query("SELECT * FROM cities")
    fun getAllTimeZones(): LiveData<List<ClockCity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertClock(clockCity: ClockCity)


    @Query("DELETE FROM cities WHERE id=:id")
    fun deleteClock(id: Int)
}

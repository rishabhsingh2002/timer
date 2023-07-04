package app.appsuccessor.sandtimer.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.appsuccessor.sandtimer.model.Alarm
import app.appsuccessor.sandtimer.model.ClockCity

@Database(entities = [ClockCity::class], version = 1, exportSchema = false)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun clockDao(): ClockDao

    companion object {
        @Volatile
        private var INSTANCE: ClockDatabase? = null

        fun getInstance(context: Context): ClockDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this)
            {
                val roomDatabaseInstance =
                    Room.databaseBuilder(context, ClockDatabase::class.java, "cities")
                        .allowMainThreadQueries().build()
                INSTANCE = roomDatabaseInstance
                return roomDatabaseInstance
            }
        }
    }
}

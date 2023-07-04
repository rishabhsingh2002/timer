package app.appsuccessor.sandtimer.datasource.local

import androidx.lifecycle.LiveData
import app.appsuccessor.sandtimer.model.Alarm
import app.appsuccessor.sandtimer.model.ClockCity

class ClockRepository(private val dao: ClockDao) {

    fun getAllCities(): LiveData<List<ClockCity>> {
        return dao.getAllTimeZones()
    }

    fun insertAlarm(clockCity: ClockCity) {
        dao.insertClock(clockCity)
    }

    fun deleteCity(id: Int) {
        dao.deleteClock(id)
    }
}
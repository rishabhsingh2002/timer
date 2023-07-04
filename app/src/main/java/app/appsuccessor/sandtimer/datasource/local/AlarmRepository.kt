package app.appsuccessor.sandtimer.datasource.local

import androidx.lifecycle.LiveData
import app.appsuccessor.sandtimer.model.Alarm

class AlarmRepository(private val dao: AlarmDao) {

    fun getAllAlarms(): LiveData<List<Alarm>> {
        return dao.getAllAlarms()
    }

    fun insertAlarm(alarm: Alarm) {
        dao.insertAlarm(alarm)
    }

    fun deleteAlarm(id: Int) {
        dao.deleteAlarm(id)
    }

    fun updateNotes(alarm: Alarm) {
        dao.updateAlarm(alarm)
    }
}
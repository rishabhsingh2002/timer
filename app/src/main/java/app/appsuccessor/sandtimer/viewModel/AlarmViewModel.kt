package app.appsuccessor.sandtimer.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import app.appsuccessor.sandtimer.datasource.local.AlarmRepository
import app.appsuccessor.sandtimer.datasource.local.AppDatabase
import app.appsuccessor.sandtimer.model.Alarm

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    init {
        val dao = AppDatabase.getInstance(application).alarmDao()
        repository = AlarmRepository(dao)
    }

    fun getAllAlarms(): LiveData<List<Alarm>> {
        return repository.getAllAlarms()
    }

    fun insertAlarm(alarm: Alarm) {
        repository.insertAlarm(alarm)
    }

    fun deleteAlarm(id: Int) {
        repository.deleteAlarm(id)
    }

    fun updateAlarm(alarm: Alarm) {
        repository.updateNotes(alarm)
    }
}
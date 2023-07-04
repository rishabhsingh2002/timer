package app.appsuccessor.sandtimer.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import app.appsuccessor.sandtimer.datasource.local.ClockDatabase
import app.appsuccessor.sandtimer.datasource.local.ClockRepository
import app.appsuccessor.sandtimer.model.ClockCity


class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClockRepository

    init {
        val dao = ClockDatabase.getInstance(application).clockDao()
        repository = ClockRepository(dao)
    }

    fun getAllCities(): LiveData<List<ClockCity>> {
        return repository.getAllCities()
    }

    fun insertCity(clockCity: ClockCity) {
        repository.insertAlarm(clockCity)
    }

    fun deleteCity(id: Int) {
        repository.deleteCity(id)
    }

}
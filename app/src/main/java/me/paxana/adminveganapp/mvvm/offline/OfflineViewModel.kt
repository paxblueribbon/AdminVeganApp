package me.paxana.adminveganapp.mvvm.offline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Observable
import me.paxana.adminveganapp.model.StateVO

class OfflineViewModel(private val offlineInteractorImpl: OfflineInteractorImpl, application: Application) : AndroidViewModel(application) {

    fun getOptionsList(): Observable<MutableList<StateVO>> {
        return offlineInteractorImpl.getOptionsList()
    }
}
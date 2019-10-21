package me.paxana.adminveganapp.mvvm.offline

import io.reactivex.Observable
import me.paxana.adminveganapp.model.StateVO

interface OfflineInteractor {
    fun getOptionsList(): Observable<MutableList<StateVO>>
}
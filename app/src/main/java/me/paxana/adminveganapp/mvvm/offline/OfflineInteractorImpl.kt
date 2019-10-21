package me.paxana.adminveganapp.mvvm.offline

import android.util.Log
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import me.paxana.adminveganapp.model.StateVO
import type.GlutenFree
import type.Vegan

class OfflineInteractorImpl: OfflineInteractor {
    override fun getOptionsList(): Observable<MutableList<StateVO>> {
        return Observable.create{
            val optionsList = mutableListOf<StateVO>()
            optionsList.add(StateVO("Select Items To View", false))
            for (enum in Vegan.values()) {
                Logger.d("Vegan", enum.name)
                optionsList.add(StateVO("Vegan: " + enum.name, false))
            }
            for (gf in GlutenFree.values()) {
                Log.d("GF", gf.name)
                optionsList.add(StateVO("GF: " + gf.name, false))
            }
        }
    }
}
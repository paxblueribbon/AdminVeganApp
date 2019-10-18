package me.paxana.adminveganapp.mvvm.aws

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import me.paxana.adminveganapp.Ingredient
import org.apache.commons.collections4.map.ListOrderedMap

class AwsViewModel(private val awsInteractorImpl: AwsInteractorImpl): ViewModel() {
    private val disposable = CompositeDisposable()
    val loading = MutableLiveData<Boolean>()

    private var ingList = mutableListOf<ListIngredientsQuery.Item>()
    val ingredientsList = MutableLiveData<List<ListIngredientsQuery.Item>>()

    fun addIngredients(nextToken: String){
        disposable.add(
            awsInteractorImpl.getIngredients(nextToken, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Response<ListIngredientsQuery.Data>>(){
                    override fun onComplete() {
                    }

                    override fun onNext(t: Response<ListIngredientsQuery.Data>) {
                        if (t.hasErrors()) {
                            Logger.e("Errors: %s", t.errors())
                        }
                        else {
                            ingList.addAll(t.data()!!.listIngredients()!!.items()!!.toMutableList())
                            ingredientsList.value = ingList
                        }
                    }

                    override fun onError(e: Throwable) {
                        Logger.e("Error: %s", e.localizedMessage)
                    }

                })
        )

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
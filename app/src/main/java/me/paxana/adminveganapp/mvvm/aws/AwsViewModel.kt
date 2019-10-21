package me.paxana.adminveganapp.mvvm.aws

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.amplify.generated.graphql.CreateIngredientMutation
import com.amazonaws.amplify.generated.graphql.DeleteIngredientMutation
import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery
import com.amazonaws.amplify.generated.graphql.UpdateIngredientMutation
import com.apollographql.apollo.api.Response
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import me.paxana.adminveganapp.model.Ingredient
import me.paxana.adminveganapp.utilities.toIngredient
import type.GlutenFree
import type.Vegan

class AwsViewModel(private val awsInteractorImpl: AwsInteractorImpl): ViewModel() {
    private val disposable = CompositeDisposable()
    val loading = MutableLiveData<Boolean>()

    private var ingList = mutableListOf<Ingredient>()
    val ingredientsList = MutableLiveData<List<Ingredient>>()

    val mutablenextToken = MutableLiveData<String>()

    fun addIngredients(){
        loading.value = true
        disposable.add(
            awsInteractorImpl.getIngredients(mutablenextToken.value, null)
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
                            t.data()!!.listIngredients()!!.items()!!.forEach {
                                val ingoo = it.toIngredient()
                                ingList.add(ingoo)
                                ingredientsList.value = ingList
                            }

//                            ingList.addAll(t.data()!!.listIngredients()!!.items()!!.toMutableList())
                            Logger.d("Next token is: %s", t.data()!!.listIngredients()!!.nextToken())
                            mutablenextToken.value = t.data()!!.listIngredients()!!.nextToken()
//                            ingredientsList.value = ingList
                            loading.value = false
                        }
                    }

                    override fun onError(e: Throwable) {
                        Logger.e("Error: %s", e.localizedMessage)
                    }
                }
                )
        )
    }

    fun changeIngredientName(oldIngredient: Ingredient, newIngredient: String, position: Int){
        disposable.add(
        awsInteractorImpl.deleteIngredient(oldIngredient.name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<Response<DeleteIngredientMutation.Data>>(){
                override fun onComplete() {
                }

                override fun onNext(t: Response<DeleteIngredientMutation.Data>) {
                    val new = Ingredient(
                        null,
                        newIngredient,
                        oldIngredient.vegan,
                        oldIngredient.gf,
                        0
                    )
                    disposable.add(awsInteractorImpl.createIngredient(new)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Response<CreateIngredientMutation.Data>>(){
                            override fun onComplete() {
                            }

                            override fun onNext(t: Response<CreateIngredientMutation.Data>) {
                                ingList.add(position, new)
                                ingredientsList.value = ingList
                            }

                            override fun onError(e: Throwable) {
                                Logger.e("Error: %s", e.localizedMessage)
                            }

                        }))
                }

                override fun onError(e: Throwable) {
                    Logger.e("Error: %s", e.localizedMessage)
                }

            })
        )

    }

    fun deleteIngredient(ingredientName: String){
        disposable.add(
        awsInteractorImpl.deleteIngredient(ingredientName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<Response<DeleteIngredientMutation.Data>>(){
                override fun onComplete() {

                }

                override fun onNext(t: Response<DeleteIngredientMutation.Data>) {
                    Logger.d("Ingredient Deleted: %s", t.toString())
                }

                override fun onError(e: Throwable) {
                    Logger.e("Error: ", e.localizedMessage)
                }

            })
        )
    }

    fun updateIngredient(ingredient: Ingredient, position: Int, newVegan: Vegan?, newGf: GlutenFree?, newName: String?){
        disposable.add(
        awsInteractorImpl.updateIngredient(ingredient)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<Response<UpdateIngredientMutation.Data>>(){
                override fun onComplete() {
                }

                override fun onNext(t: Response<UpdateIngredientMutation.Data>) {
                    if (newVegan != null) {
                        Logger.d("Position is %s", position)
                        ingList[position].vegan = newVegan
                    }
//                    if (newGf != null) {}
//                    if (newName != null) {}
                    Logger.d("Updated Ingredient: %s", t.data().toString())

//                    ingList.removeAt(position)
//                    ingList.add(position, ingredient)
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
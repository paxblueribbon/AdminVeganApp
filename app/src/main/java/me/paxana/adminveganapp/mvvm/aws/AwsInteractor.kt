package me.paxana.adminveganapp.mvvm.aws

import com.amazonaws.amplify.generated.graphql.*
import com.apollographql.apollo.api.Response
import io.reactivex.Observable
import me.paxana.adminveganapp.Ingredient
import type.CreateIngredientInput
import type.TableIngredientFilterInput

interface AwsInteractor {
    fun createIngredient(ingredient: Ingredient): Observable<Response<CreateIngredientMutation.Data>>
    fun updateIngredient(ingredient: Ingredient): Observable<Response<UpdateIngredientMutation.Data>>
    fun deleteIngredient(ingredientName: String): Observable<Response<DeleteIngredientMutation.Data>>
    fun getIngredients(nextToken: String?, filter: TableIngredientFilterInput?): Observable<Response<ListIngredientsQuery.Data>>
    fun getAnIngredient(name: String): Observable<Response<GetIngredientQuery.Data>>
}
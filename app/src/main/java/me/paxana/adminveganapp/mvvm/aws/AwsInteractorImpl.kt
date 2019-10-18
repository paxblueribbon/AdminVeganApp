package me.paxana.adminveganapp.mvvm.aws

import com.amazonaws.amplify.generated.graphql.*
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import me.paxana.adminveganapp.ClientFactory
import me.paxana.adminveganapp.Ingredient
import type.*
import javax.annotation.Nonnull

class AwsInteractorImpl: AwsInteractor {

    override fun createIngredient(ingredient: Ingredient): Observable<Response<CreateIngredientMutation.Data>> {
            return Observable.create { emitter ->
                ClientFactory.appSyncClient()!!.mutate(CreateIngredientMutation(
                    CreateIngredientInput.builder().name(ingredient.name).vegan(ingredient.vegan).glutenfree(ingredient.gf).build()))
                    .enqueue(object : GraphQLCall.Callback<CreateIngredientMutation.Data>(){
                        override fun onFailure(e: ApolloException) {
                            Logger.e("Err: %s", e.localizedMessage)
                        }

                        override fun onResponse(response: Response<CreateIngredientMutation.Data>) {
                            emitter.onNext(response)
                        }
                    })
            }
    }

    override fun updateIngredient(ingredient: Ingredient): Observable<Response<UpdateIngredientMutation.Data>> {
        return Observable.create { emitter ->
            val updateIngredientInput = UpdateIngredientInput.builder().name(ingredient.name).vegan(ingredient.vegan).glutenfree(ingredient.gf).popularity(0).build()
            ClientFactory.appSyncClient()!!.mutate(UpdateIngredientMutation.builder().input(updateIngredientInput).build())
                .enqueue(object : GraphQLCall.Callback<UpdateIngredientMutation.Data>(){
                    override fun onFailure(e: ApolloException) {
                        Logger.e("Err %s", e.localizedMessage)
                    }

                    override fun onResponse(response: Response<UpdateIngredientMutation.Data>) {
                        emitter.onNext(response)
                    }

                })
        }
    }

    override fun deleteIngredient(ingredientName: String): Observable<Response<DeleteIngredientMutation.Data>> {
        return Observable.create{ emitter ->
            val deleteIngredientInput = DeleteIngredientInput.builder().name(ingredientName).build()
            ClientFactory.appSyncClient()!!.mutate(DeleteIngredientMutation.builder().input(deleteIngredientInput).build())
                .enqueue(object : GraphQLCall.Callback<DeleteIngredientMutation.Data>(){
                    override fun onFailure(e: ApolloException) {
                        Logger.e("Error: %s", e.localizedMessage)
                    }

                    override fun onResponse(response: Response<DeleteIngredientMutation.Data>) {
                        emitter.onNext(response)
                    }
                })
        }

    }

    override fun getIngredients(nextToken: String?, filter: TableIngredientFilterInput?): Observable<Response<ListIngredientsQuery.Data>> {
        return Observable.create{ emitter ->
            ClientFactory.appSyncClient()!!.query(ListIngredientsQuery.builder()
                .filter(filter)
                .nextToken(nextToken).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(object : GraphQLCall.Callback<ListIngredientsQuery.Data>(){
                    override fun onFailure(e: ApolloException) {
                        Logger.e("Error: %s", e.localizedMessage)
                    }

                    override fun onResponse(response: Response<ListIngredientsQuery.Data>) {
                        emitter.onNext(response)
                    }
                })
        }
    }

    override fun getAnIngredient(name: String): Observable<Response<GetIngredientQuery.Data>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
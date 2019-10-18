package me.paxana.adminveganapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.*
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.snackbar.Snackbar
import com.paginate.Paginate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.paxana.adminveganapp.mvvm.aws.AwsInteractorImpl
import me.paxana.adminveganapp.mvvm.aws.AwsViewModel
import me.paxana.adminveganapp.utilities.getViewModel
import type.*
import javax.annotation.Nonnull

class MainActivity : AppCompatActivity() {

    val test: String = "Test"

    private val awsViewModel: AwsViewModel by lazy{
        getViewModel{ AwsViewModel(AwsInteractorImpl()) }
    }

    var metaIngList = mutableListOf<ListIngredientsQuery.Item>()
    private lateinit var veganItemSelectListener: RecyclerViewClickListener
    var userIsInteracting: Boolean = false
    private lateinit var adapter: IngredientRecyclerAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    var mLoading = false
    var nextToken: String? = null
    private var paginationHasBeenSetup = false
    private var rvHasBeenInitialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        ClientFactory.init(this)


        veganItemSelectListener = object : RecyclerViewClickListener {
            override fun onDeleteSelect(ingredientName: String, position: Int) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Are you sure?")
                builder.setMessage("Are you sure you want to delete $ingredientName?")
                builder.setPositiveButton("I'm sure."){dialog, which ->
                    Toast.makeText(this@MainActivity, "Deleted $ingredientName at position ${position.toString()}", Toast.LENGTH_LONG).show()
                    removeIngredient(ingredientName)
                    adapter.notifyItemRemoved(position) }
                builder.setNegativeButton("No, nevermind") {dialog, which ->  Log.d("not deleted", "not baleeted")}

                builder.create().show()
            }

            override fun onConfirmSelect(oldIngredient: String, newIngredient: String, position: Int) {
                if (oldIngredient.compareTo(newIngredient) != 0) {
                    removeIngredient(oldIngredient)
                    createIngredient(Ingredient(null, newIngredient, Vegan.UNKNOWN, GlutenFree.UNKNOWN, 0))
                    metaIngList[position] = ListIngredientsQuery.Item("Ingredient", newIngredient, null, Vegan.UNKNOWN, GlutenFree.UNKNOWN, 0)
                    adapter.notifyItemChanged(position)
                }
            }

            override fun onGfItemSelect(ingredient: ListIngredientsQuery.Item, selection: GlutenFree, position: Int) {
                if (userIsInteracting) {
                    Log.d("gf item selected", "yup")
                    val newIng = Ingredient(null, ingredient.name(), ingredient.vegan()!!, selection, 0)
                    metaIngList[position] = ListIngredientsQuery.Item("Ingredient", newIng.name, null, newIng.vegan, selection, 0)
                    updateIngredient(newIng)
                    adapter.notifyItemChanged(position)
                }
            }

            override fun onVeganItemSelect(ingredient: ListIngredientsQuery.Item, selection: Vegan, position: Int) {
                if (userIsInteracting) {
                    Log.d("vegan item selected", "yup")
                    val newIng = Ingredient(null, ingredient.name(), selection, ingredient.glutenfree()!!, 0)
                    metaIngList[position] = ListIngredientsQuery.Item("Ingredient", newIng.name, null, selection, newIng.glutenfree(), 0)
                    updateIngredient(newIng)
                    adapter.notifyItemChanged(position)
                }
            }
        }

        getIngredient("rooibos")

        settingsSpinner.adapter = MyAdapter(this@MainActivity, 0, getOptionsList() )

        getIngredients(null)

        submitButton.setOnClickListener {  }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private val mutationCallback = object: GraphQLCall.Callback<CreateIngredientMutation.Data>() {
        override fun onResponse(response: Response<CreateIngredientMutation.Data>) {
            Log.i("Results", "Added Ingredient")
        }
        override fun onFailure(@Nonnull e: ApolloException) {
            Log.e("Error", e.toString())
        }
    }

    private val updateMutationCallback = object: GraphQLCall.Callback<UpdateIngredientMutation.Data>(){
        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }

        override fun onResponse(response: Response<UpdateIngredientMutation.Data>) {
            Log.d("Updated", response.toString())
        }
    }

    var callbacks = object : Paginate.Callbacks {
        override fun onLoadMore() {
            mLoading = true
            Log.d("Getting Ingredients", "now")

            getIngredients(nextToken)

//           ClientFactory.appSyncClient()!!.query(ListIngredientsQuery.builder().nextToken(nextToken).build())
//                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
//                .enqueue(ingredientsCallback2)
        }

        override fun isLoading(): Boolean {
            return mLoading
        }

        override fun hasLoadedAllItems(): Boolean {
            //TODO: Make it check if there are no more items
            return false
        }

    }

    fun createIngredient(ingredient: Ingredient) {
        val createIngredientInput = CreateIngredientInput.builder().name(ingredient.name)
            .vegan(ingredient.vegan)
            .glutenfree(ingredient.gf)
            .popularity(0)
            .build()
        ClientFactory.appSyncClient()!!.mutate(CreateIngredientMutation.builder().input(createIngredientInput).build())
            .enqueue(mutationCallback)
    }

    fun updateIngredient(ingredient: Ingredient) {
        val updateIngredientInput = UpdateIngredientInput.builder().name(ingredient.name)
            .vegan(ingredient.vegan)
            .glutenfree(ingredient.gf)
            .popularity(0)
            .build()
            ClientFactory.appSyncClient()!!.mutate(UpdateIngredientMutation.builder().input(updateIngredientInput).build())
            .enqueue(updateMutationCallback)
    }

    fun removeIngredient(ingredientName: String) {
        Log.d("Deleting ingredient:", ingredientName)
        val deleteIngredientInput = DeleteIngredientInput.builder().name(ingredientName).build()

        ClientFactory.appSyncClient()!!.mutate(DeleteIngredientMutation.builder().input(deleteIngredientInput).build())
            .enqueue(deleteMutationCallback)
    }

    private val deleteMutationCallback = object: GraphQLCall.Callback<DeleteIngredientMutation.Data>(){
        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }

        override fun onResponse(response: Response<DeleteIngredientMutation.Data>) {
            Log.d("Updated", response.toString())
        }

    }

    private fun getIngredients(nextToken: String?) {
        Log.d("Getting Ingredients", "now")
        ClientFactory.appSyncClient()!!.query(ListIngredientsQuery.builder()
            .filter(TableIngredientFilterInput.builder().name(TableStringFilterInput.builder().contains("ham").build()).build())
            .nextToken(nextToken).build())
            .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
            .enqueue(ingredientsCallback)
    }

    private fun getIngredient(name: String) {
        ClientFactory.appSyncClient()!!.query(GetIngredientQuery.builder().name(name).build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(oneIngCallback)
    }

    private val oneIngCallback: GraphQLCall.Callback<GetIngredientQuery.Data> = object :GraphQLCall.Callback<GetIngredientQuery.Data>() {
        override fun onFailure(e: ApolloException) {
            runOnUiThread {
                Toast.makeText(applicationContext, "DIDN'T GET IT", Toast.LENGTH_SHORT).show()
                Log.e("err", e.localizedMessage)
            }

        }

        override fun onResponse(response: Response<GetIngredientQuery.Data>) {
            runOnUiThread {
                Toast.makeText(applicationContext, "GOT IT", Toast.LENGTH_SHORT).show()
                Log.d("success", response.toString())
            }

        }

    }

    private val ingredientsCallback = object:GraphQLCall.Callback<ListIngredientsQuery.Data>() {
        override fun onResponse(response: Response<ListIngredientsQuery.Data>) {
            if (response.hasErrors()) {
                Log.e("errors", "everywhere")
            }
            else {
                metaIngList.addAll(response.data()!!.listIngredients()!!.items()!!.toMutableList())
//            TODO: Create map from data instead of storing list
                Log.d("Got Ingredients", "Now")
                nextToken = response.data()?.listIngredients()?.nextToken()
                Log.d("Next token is: ", nextToken)
                Log.d("Number of ing remote: ", metaIngList.size.toString())
                metaIngList.forEach {
                                    Log.d("Ingg", it.name())
                }
                runOnUiThread {
                    if (!rvHasBeenInitialized) {
                        initRecyclerView(metaIngList)
                    }
                    else {
                        runOnUiThread{
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

        }
        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }
    }

    private val ingredientsCallback2 = object:GraphQLCall.Callback<ListIngredientsQuery.Data>() {
        override fun onFailure(e: ApolloException) {
            Log.e("Error", e.toString())
        }

        override fun onResponse(response: Response<ListIngredientsQuery.Data>) {
            nextToken = response.data()?.listIngredients()?.nextToken()
            val newList = response.data()!!.listIngredients()!!.items()!!.toMutableList()
            val position = metaIngList.size - 1
            metaIngList.addAll(newList)

            runOnUiThread { ingredient_rv.adapter!!.notifyItemInserted(position)}

            mLoading = false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupPagination() {

        Paginate.with(ingredient_rv, callbacks)
            .setLoadingTriggerThreshold(2)
            .addLoadingListItem(true)
            .setLoadingListItemCreator(null)
            .setLoadingListItemSpanSizeLookup{ 1 }
            .build()
        paginationHasBeenSetup = true
    }

    private fun getOptionsList(): MutableList<StateVO> {
        var optionsList = mutableListOf<StateVO>()
        optionsList.add(StateVO("Select Items To View", false))
        for (enum in Vegan.values()) {
            Log.d("Vegan", enum.name)
            optionsList.add(StateVO("Vegan: " + enum.name, false))
        }
        for (gf in GlutenFree.values()) {
            Log.d("GF", gf.name)
            optionsList.add(StateVO("GF: " + gf.name, false))
        }
        return optionsList
    }


    fun initRecyclerView(ingList: List<ListIngredientsQuery.Item>) {
        Log.d("initRV", "Running")

        rvHasBeenInitialized = true

        linearLayoutManager = LinearLayoutManager(this)
        adapter = IngredientRecyclerAdapter(ingList, veganItemSelectListener)
        ingredient_rv.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(this , linearLayoutManager.orientation)
        ingredient_rv.addItemDecoration(dividerItemDecoration)
        ingredient_rv.adapter = adapter
        if (!paginationHasBeenSetup) {
            setupPagination()
        }
    }

}

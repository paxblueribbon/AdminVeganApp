package me.paxana.adminveganapp.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.amplify.generated.graphql.*
import com.google.android.material.snackbar.Snackbar
import com.paginate.Paginate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.paxana.adminveganapp.mvvm.aws.AwsInteractorImpl
import me.paxana.adminveganapp.mvvm.aws.AwsViewModel
import me.paxana.adminveganapp.utilities.getViewModel
import type.*
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger.addLogAdapter
import androidx.lifecycle.Observer
import com.orhanobut.logger.Logger
import me.paxana.adminveganapp.model.Ingredient
import me.paxana.adminveganapp.adapters.IngredientRecyclerAdapter
import me.paxana.adminveganapp.R
import me.paxana.adminveganapp.adapters.RecyclerViewClickListener
import me.paxana.adminveganapp.utilities.ClientFactory
import me.paxana.adminveganapp.utilities.toIngredient


class MainActivity : AppCompatActivity() {

    private val awsViewModel: AwsViewModel by lazy{
        getViewModel{ AwsViewModel(AwsInteractorImpl()) }
    }

    var userIsInteracting: Boolean = false
    private val adapter = IngredientRecyclerAdapter(
        mutableListOf(),
        object : RecyclerViewClickListener {
            override fun onVeganItemSelect(
                ingredient: ListIngredientsQuery.Item,
                selection: Vegan,
                position: Int
            ) {
                if (userIsInteracting) {
                    if (ingredient.vegan() != selection) {
                        Logger.d("vegan item selected")

                        awsViewModel.updateIngredient(
                            ingredient.toIngredient(),
                            position,
                            selection,
                            null,
                            null
                        )
                    }
                }
            }

            override fun onGfItemSelect(
                ingredient: ListIngredientsQuery.Item,
                selection: GlutenFree,
                position: Int
            ) {
                if (userIsInteracting) {
                    if (ingredient.glutenfree() != selection) {
                        Logger.d("gf item selected")
                        awsViewModel.updateIngredient(
                            ingredient.toIngredient(),
                            position,
                            null,
                            selection,
                            null
                        )
                    }
                }
            }

            override fun onConfirmSelect(
                oldIngredient: Ingredient,
                newIngredient: String,
                position: Int
            ) {
                if (oldIngredient.name.compareTo(newIngredient) != 0) {
                    awsViewModel.changeIngredientName(oldIngredient, newIngredient, position)
                }
            }

            override fun onDeleteSelect(ingredientName: String, position: Int) {
                //TODO: change string literals to values
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Are you sure?")
                builder.setMessage("Are you sure you want to delete $ingredientName?")
                builder.setPositiveButton("I'm sure.") { dialog, which ->
                    Toast.makeText(
                        this@MainActivity,
                        "Deleted $ingredientName at position $position",
                        Toast.LENGTH_LONG
                    ).show()
                    awsViewModel.deleteIngredient(ingredientName)
                }
                builder.setNegativeButton("No, nevermind") { dialog, which ->
                    Log.d(
                        "not deleted",
                        "not baleeted"
                    )
                }
                builder.create().show()
            }
        })
    private lateinit var linearLayoutManager: LinearLayoutManager
    var mLoading = false
    var nextToken: String? = null
    private var paginationHasBeenSetup = false
    private var rvHasBeenInitialized = false

    private val loadingObserver = Observer<Boolean> {
        mLoading = it
    }

    private val ingObserver = Observer<List<Ingredient>> {
        it.let {
            if (!rvHasBeenInitialized) {
                initRecyclerView()
                runOnUiThread {
                    adapter.addIngs(it)
                }
            }
            else {
                runOnUiThread {
                    adapter.addIngs(it)
                }
            }
            Logger.d("List is now %s", it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        ClientFactory.init(this)
        addLogAdapter(AndroidLogAdapter())

        awsViewModel.ingredientsList.observe(this, ingObserver)
        awsViewModel.loading.observe(this, loadingObserver)

//        settingsSpinner.adapter = SpinnerOptionsAdapter(this@MainActivity, 0, getOptionsList() )

        awsViewModel.addIngredients()

        submitButton.setOnClickListener {  }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    var callbacks = object : Paginate.Callbacks {
        override fun onLoadMore() {
            mLoading = true
            Log.d("Getting Ingredients", "now")

            awsViewModel.addIngredients()
        }

        override fun isLoading(): Boolean {
            return mLoading
        }

        override fun hasLoadedAllItems(): Boolean {
            //TODO: Make it check if there are no more items
            return false
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

    private fun initRecyclerView() {
        Log.d("initRV", "Running")

        rvHasBeenInitialized = true

        runOnUiThread {
            linearLayoutManager = LinearLayoutManager(this)
            ingredient_rv.layoutManager = linearLayoutManager
            val dividerItemDecoration = DividerItemDecoration(this , linearLayoutManager.orientation)
            ingredient_rv.addItemDecoration(dividerItemDecoration)
            ingredient_rv.adapter = adapter
            if (!paginationHasBeenSetup) {
                setupPagination()
            }
        }
    }

}

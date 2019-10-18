package me.paxana.adminveganapp.utilities

import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.amazonaws.amplify.generated.graphql.GetIngredientQuery
import me.paxana.adminveganapp.Ingredient

fun List<String>.toIngredients(): List<Ingredient> {
    val ingList = mutableListOf<Ingredient>()
    this.forEach {
        val ing = Ingredient("id", it, type.Vegan.UNKNOWN, type.GlutenFree.UNKNOWN, 0)
        ingList.add(ing)
    }
    return ingList
}

fun String.toAllCaps(): String {
    return this.toUpperCase()
}

fun GetIngredientQuery.GetIngredient.toIngredient(): Ingredient {
    return Ingredient(id()!!, name(), vegan()!!, glutenfree()!!, 0)
}

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}
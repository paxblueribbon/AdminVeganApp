package me.paxana.adminveganapp.adapters

import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery
import me.paxana.adminveganapp.model.Ingredient
import type.GlutenFree
import type.Vegan

interface RecyclerViewClickListener {
    fun onVeganItemSelect(ingredient: ListIngredientsQuery.Item, selection: Vegan, position: Int)
    fun onGfItemSelect(ingredient: ListIngredientsQuery.Item, selection: GlutenFree, position: Int)
    fun onConfirmSelect(oldIngredient: Ingredient, newIngredient: String, position: Int)
    fun onDeleteSelect(ingredientName: String, position: Int)

}
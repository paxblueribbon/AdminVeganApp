package me.paxana.adminveganapp

import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery
import type.CreateIngredientInput
import type.DeleteIngredientInput
import type.GlutenFree
import type.Vegan

interface RecyclerViewClickListener {
    fun onVeganItemSelect(ingredient: ListIngredientsQuery.Item, selection: Vegan, position: Int)
    fun onGfItemSelect(ingredient: ListIngredientsQuery.Item, selection: GlutenFree, position: Int)
    fun onConfirmSelect(oldIngredient: String, newIngredient: String, position: Int)
    fun onDeleteSelect(ingredientName: String, position: Int)

}
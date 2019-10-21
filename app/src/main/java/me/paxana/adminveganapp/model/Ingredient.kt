package me.paxana.adminveganapp.model

import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery

data class Ingredient(val id: String?, var name: String, var vegan: type.Vegan, var gf: type.GlutenFree, val popularity: Int ) :
    ListIngredientsQuery.Item("Ingredient", name, id, vegan, gf, popularity)
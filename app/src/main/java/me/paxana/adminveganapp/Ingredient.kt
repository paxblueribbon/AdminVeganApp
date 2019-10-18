package me.paxana.adminveganapp

import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery

data class Ingredient(val id: String?, var name: String, val vegan: type.Vegan, val gf: type.GlutenFree, val popularity: Int ) :
    ListIngredientsQuery.Item("Ingredient", name, id, vegan, gf, popularity)
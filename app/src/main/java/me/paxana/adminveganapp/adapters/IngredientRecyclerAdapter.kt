package me.paxana.adminveganapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListIngredientsQuery
import kotlinx.android.synthetic.main.ingredientlayout.view.*
import me.paxana.adminveganapp.R
import me.paxana.adminveganapp.model.Ingredient
import me.paxana.adminveganapp.utilities.toIngredient
import type.GlutenFree
import type.Vegan

class IngredientRecyclerAdapter(private var ingredients: List<Ingredient>, private val clickListener: RecyclerViewClickListener) : RecyclerView.Adapter<IngredientRecyclerAdapter.IngHolder>() {


    fun addIngs(newIngList: List<Ingredient>){
        ingredients = newIngList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IngHolder {
      val inflatedView: View = LayoutInflater.from(p0.context).inflate(R.layout.ingredientlayout, p0, false)
        return IngHolder(inflatedView)
    }


    override fun getItemCount(): Int {
        return ingredients.size
    }

    override fun onBindViewHolder(holder: IngHolder, position: Int) {
        val ingredient = ingredients[position]
        val clickListener = clickListener
        val item = holder.adapterPosition
        holder.bindIngredient(ingredient, clickListener)
    }

    class IngHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var view : View = v
        private var context: Context = v.context
        private var ingredient : ListIngredientsQuery.Item? = null

        init {
            v.veganSpinner.adapter = VeganDropDownAdapter(context)
            v.gfSpinner.adapter = GfDropDownAdapter(context)
        }

        companion object {
            private val INGREDIENT_KEY = "INGREDIENT"
        }



        fun bindIngredient(ingredient: ListIngredientsQuery.Item, clickListener: RecyclerViewClickListener) {

            var isEditable: State =
                State.CONFIRMED
            var oldIng: Ingredient? = null
            var newName: String

            val ocl1 = View.OnClickListener{
                //Text Editable

                if (isEditable == State.CONFIRMED) {
                    oldIng = this.ingredient!!.toIngredient()
                    view.ingEditText.setText(view.ingNameTV.text.toString())
                    view.ingNameTV.visibility = View.INVISIBLE
                    view.ingEditText.visibility = View.VISIBLE
                    view.ingEditButton.text = "Confirm"
                    isEditable =
                        State.EDITABLE
                }

                else if (isEditable == State.EDITABLE) {
                    newName = view.ingEditText.text.toString()
                    view.ingNameTV.text = newName
                    view.ingEditText.visibility = View.INVISIBLE
                    view.ingNameTV.visibility = View.VISIBLE
                    Log.d("Adapter Position", adapterPosition.toString())
                    clickListener.run { onConfirmSelect(oldIng!!, newName, adapterPosition) }
                    view.ingEditButton.text = "Edit"
                    isEditable =
                        State.CONFIRMED
                }
            }

            view.deleteButton.setOnClickListener { clickListener.onDeleteSelect(ingredient.name(), adapterPosition) }

            this.ingredient = ingredient
            view.ingNameTV.text = ingredient.name()
            view.ingEditButton.setOnClickListener(ocl1)
            view.veganSpinner.setSelection(Vegan.valueOf(ingredient.vegan().toString()).ordinal, false)
            view.gfSpinner.setSelection(GlutenFree.valueOf(ingredient.glutenfree().toString()).ordinal, false)

            view.veganSpinner.onItemSelectedListener = object : OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    clickListener.onVeganItemSelect(ingredient, Vegan.values()[position], adapterPosition)
                }
            }
            view.gfSpinner.onItemSelectedListener = object : OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    clickListener.onGfItemSelect(ingredient, GlutenFree.values()[position], adapterPosition)
                }
            }
        }
    }

    enum class State {
        EDITABLE, CONFIRMED
    }

}

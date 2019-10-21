package me.paxana.adminveganapp.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.view.LayoutInflater
import me.paxana.adminveganapp.R
import me.paxana.adminveganapp.model.StateVO

class SpinnerOptionsAdapter(contexxt: Context, resource:Int, objects:List<StateVO>): ArrayAdapter<StateVO>(contexxt, resource, objects) {
    private val listState:ArrayList<StateVO> = objects as ArrayList<StateVO>
    private var isFromView = false
    override fun getDropDownView(position:Int, convertView: View?, parent: ViewGroup): View? {
        return getCustomView(position, convertView, parent)
    }
    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View? {
        return getCustomView(position, convertView, parent)
    }
    private fun getCustomView(position:Int, convertView:View?, parent: ViewGroup):View? {
        val holder: ViewHolder
        var cv :View? = convertView
        if (cv == null)
        {
            val layoutInflator = LayoutInflater.from(context)
            cv = layoutInflator.inflate(R.layout.spinner_item, parent, false)
            holder = ViewHolder()
            holder.mTextView = cv.findViewById(R.id.text) as TextView
            holder.mCheckBox = cv.findViewById(R.id.checkbox) as CheckBox
            cv.tag = holder
        }
        else
        {
            holder = cv.tag as ViewHolder
        }
        holder.mTextView.text = listState[position].title
        // To check whether checked event fire from getview() or user input
        isFromView = true
        holder.mCheckBox.isChecked = listState[position].isSelected
        isFromView = false
        if ((position == 0))
        {
            holder.mCheckBox.visibility = View.INVISIBLE
        }
        else
        {
            holder.mCheckBox.visibility = View.VISIBLE
        }
        holder.mCheckBox.tag = position
        holder.mCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val getPosition = buttonView.tag as Int
            if (!isFromView) {
                listState[position].isSelected = (isChecked)
            }
        }
        return cv
    }
    class ViewHolder {
        lateinit var mTextView:TextView
        lateinit var mCheckBox:CheckBox
    }
}
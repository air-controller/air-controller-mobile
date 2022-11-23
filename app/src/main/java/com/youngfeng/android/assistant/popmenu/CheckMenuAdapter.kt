package com.youngfeng.android.assistant.popmenu

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.skydoves.powermenu.MenuBaseAdapter
import com.youngfeng.android.assistant.R

class CheckMenuAdapter : MenuBaseAdapter<CheckMenuItem>() {

    override fun getView(index: Int, view: View?, viewGroup: ViewGroup?): View {
        val context: Context = viewGroup!!.context

        var currentView = view
        if (currentView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            currentView = layoutInflater.inflate(R.layout.check_menu_item, viewGroup, false)
        }

        currentView?.apply {
            val item = getItem(index) as CheckMenuItem
            currentView.findViewById<TextView>(R.id.text_title).text = item.text

            if (item.isChecked) {
                currentView.setBackgroundColor(Color.parseColor("#e6f3fe"))
                currentView.findViewById<TextView>(R.id.text_title).setTextColor(Color.parseColor("#0c89ff"))
                currentView.findViewById<ImageView>(R.id.image_checked).visibility = View.VISIBLE
            } else {
                currentView.setBackgroundColor(Color.WHITE)
                currentView.findViewById<TextView>(R.id.text_title).setTextColor(Color.parseColor("#010101"))
                currentView.findViewById<ImageView>(R.id.image_checked).visibility = View.GONE
            }
        }

        return super.getView(index, currentView, viewGroup)
    }
}

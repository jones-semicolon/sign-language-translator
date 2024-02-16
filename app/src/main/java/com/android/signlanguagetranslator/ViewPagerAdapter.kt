package com.android.signlanguagetranslator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter


class ViewPagerAdapter(context: Context) : PagerAdapter() {
    var context: Context
    var images = intArrayOf(
        R.mipmap.ic_launcher_new,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4
    )
    private var headings = context.resources.getStringArray(R.array.headers)
    var description = context.resources.getStringArray(R.array.description)

    init {
        this.context = context
    }

    override fun getCount(): Int {
        return headings.size
    }

    override fun isViewFromObject(view: View,obj: Any): Boolean {
        return view === obj as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_slider, container, false)
        val slideTitleImage = view.findViewById<ImageView>(R.id.titleImage)
        val slideHeading = view.findViewById<TextView>(R.id.texttitle)
        val slideDescription = view.findViewById<TextView>(R.id.textdescription)

        slideTitleImage.setImageResource(images[position])
        slideHeading.setText(headings[position])
        slideDescription.setText(description[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }
}
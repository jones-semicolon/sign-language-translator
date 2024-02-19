package com.android.signlanguagetranslator

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback


class ViewPagerAdapter(context: Context) : PagerAdapter() {
    var context: Context

    var images = arrayOf(
        R.drawable.ic_logo,
        "person_signing_d",
        arrayOf(
            "person_signing_d",
            "camera",
        ),
        "person_with_phone",
        "settings"
    )
    private var headlines = context.resources.getStringArray(R.array.instruction)

    init {
        this.context = context
    }

    override fun getCount(): Int {
        return headlines.size
    }

    override fun isViewFromObject(view: View,obj: Any): Boolean {
        return view === obj as LinearLayout
    }

    // Define layout parameters for the LottieAnimationView
    val layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val typedValue = TypedValue()
        val theme = context.theme
        val isFound = theme.resolveAttribute(R.attr.lottieColor, typedValue, true)
        Log.d("THEME", "data = ${typedValue.data}")
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_slider, container, false)
        val lottieContainer = view.findViewById<RelativeLayout>(R.id.lottieContainer)
        val slideHeading = view.findViewById<TextView>(R.id.texttitle)

        if(images[position] is Int) {
            val imageView = ImageView(context).apply {
                setImageResource(images[position] as Int)
            }
            lottieContainer.addView(imageView)
        } else if (images[position] is String){
            Log.d("LOTTIE", images[position].toString())
            lottieContainer.addView(
                LottieAnimationView(context).apply {
                    this.setAnimation(context.resources.getIdentifier(images[position] as String, "raw", context.packageName))
                    this.repeatCount = LottieDrawable.INFINITE
                    this.playAnimation()
                }, layoutParams
            )
        } else if (images[position] is Array<*>){
            (images[position] as Array<*>).forEachIndexed { index, item ->
                Log.d("LOTTIE", item.toString())
                lottieContainer.addView(
                    LottieAnimationView(context).apply {
                        this.setAnimation(context.resources.getIdentifier(item as String, "raw", context.packageName))
                        if(index == 1){
                        this.addValueCallback(
                            KeyPath("**"), // Apply to all layers
                            LottieProperty.COLOR_FILTER,
                            LottieValueCallback<ColorFilter>(PorterDuffColorFilter(typedValue.data, PorterDuff.Mode.SRC_ATOP))
                        )}
                        this.repeatCount = LottieDrawable.INFINITE
                        this.playAnimation()
                    }, layoutParams
                )
            }
        }


        /*lottieContainer.addView(LottieAnimationView(context).apply {
            this.setAnimation(R.raw.person_with_phone)
            this.repeatCount = LottieDrawable.INFINITE
            this.playAnimation()
        })
*/
        slideHeading.text = headlines[position]
        if (position == 0) slideHeading.textSize = 28f else slideHeading.textSize = 16f



        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }
}
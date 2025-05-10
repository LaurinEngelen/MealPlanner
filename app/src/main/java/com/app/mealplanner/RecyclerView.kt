package com.app.mealplanner

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class NonScrollableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        // Prevent scrolling by setting the height to the measured height
        layoutParams.height = measuredHeight
    }

    override fun scrollBy(x: Int, y: Int) {
        // Disable vertical scrolling
        if (y != 0) return
        super.scrollBy(x, y)
    }
}
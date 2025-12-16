package com.example.softhats.ui.profile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AvatarAdapter(
    private val context: Context,
    private val avatars: List<Int>
) : BaseAdapter() {

    override fun getCount(): Int = avatars.size

    override fun getItem(position: Int): Any = avatars[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = convertView as? ImageView ?: ImageView(context)

        imageView.setImageResource(avatars[position])
        imageView.layoutParams = ViewGroup.LayoutParams(220, 220)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setPadding(16, 16, 16, 16)

        return imageView
    }
}

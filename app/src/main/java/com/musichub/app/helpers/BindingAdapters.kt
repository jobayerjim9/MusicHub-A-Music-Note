package com.musichub.app.helpers

import android.graphics.Color
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.musichub.app.R
import com.squareup.picasso.Picasso

class BindingAdapters {
    companion object {
        @JvmStatic @BindingAdapter("app:loadImage")
        fun loadImage(imageView: ImageView, url: String?) {
            if (url.toString().isNotEmpty()) {
            Picasso.get().load(url).into(imageView)
            }
        }

        @JvmStatic @BindingAdapter("app:cardBackgroundColor")
        fun setCardBackgroundColor(cardView: CardView, color: String?) {
            if (color != null) {
                cardView.setCardBackgroundColor(Color.parseColor(color))
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }
    }
}
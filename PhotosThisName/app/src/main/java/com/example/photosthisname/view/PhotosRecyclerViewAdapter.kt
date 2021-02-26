package com.example.photosthisname.view

import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.photosthisname.R
import com.example.photosthisname.data.Photo
import com.example.photosthisname.databinding.PhotoItemBinding
import java.lang.Exception

class PhotosRecyclerViewAdapter : RecyclerView.Adapter<PhotosRecyclerViewAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root)

    var photos: MutableList<Photo> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var loadMorePhotos: () -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = PhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        with(holder) {
            setPhoto(binding.photoImageView, photos[position].url_s)
            setPhotoClickListener(binding.photoImageView, photos[position].url_m) // clicking on photo opens it in medium size in browser
            binding.ownerNameTextView.text = photos[position].ownerName
            binding.dateTakenTextView.text = photos[position].dateTaken
            val description = photos[position].description.content
            binding.descriptionTextView.text =
                if (description.isNullOrEmpty()) holder.itemView.context.getString(R.string.none)
                else Html.fromHtml(photos[position].description.content, Html.FROM_HTML_MODE_COMPACT)

            if (itemCount - 1 == position) { // if arrive to the end of the photos list load more photos
                loadMorePhotos()
            }
        }
    }

    override fun getItemCount() = photos.size

    private fun setPhoto(photoImageView: ImageView, photoUrl: String) = Glide
            .with(photoImageView.context)
            .load(photoUrl)
            .error(R.drawable.n_a)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(photoImageView)

    private fun setPhotoClickListener(photoImageView: ImageView, photoUrl: String) = photoImageView.setOnClickListener {
        // clicking on photo opens it in medium size in browser
        try {
            val builder = CustomTabsIntent.Builder()
            val customParams = CustomTabColorSchemeParams.Builder().setToolbarColor(Color.parseColor("#FF6200EE")).build()
            builder.setDefaultColorSchemeParams(customParams)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(photoImageView.context, Uri.parse(photoUrl))
        } catch (e: Exception) {
            Log.w("PhotosNameApp", "PhotosRecyclerViewAdapter.setPhotoClickListener - Exception, error=${e.message}")
        }
    }
}

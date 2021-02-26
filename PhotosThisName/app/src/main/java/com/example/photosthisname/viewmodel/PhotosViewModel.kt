package com.example.photosthisname.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.example.photosthisname.data.Photo
import com.example.photosthisname.data.PhotosData
import kotlinx.coroutines.Dispatchers

class PhotosViewModel(application: Application) : AndroidViewModel(application) {

    private var currentPhotosList = mutableListOf<Photo>()
    private var currentPage = 3 // initially we get photos from the first three pages
    private lateinit var currentTags: String

    fun getPhotos(tags: String) = liveData(Dispatchers.IO) {
        currentPhotosList = PhotosData.getPhotos(tags).toMutableList() // get photos from the first three pages
        currentTags = tags
        emit(currentPhotosList)
    }

    fun loadMorePhotos() = liveData(Dispatchers.IO) {
        if (::currentTags.isInitialized && currentPage < PhotosData.pages) { // if it's the last page cannot load more photos
            currentPage++
            currentPhotosList.addAll(PhotosData.getPhotos(currentTags, currentPage))
            emit(currentPhotosList)
        }
    }
}

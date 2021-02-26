package com.example.photosthisname.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object PhotosData {

    const val apiKey = "aabca25d8cd75f676d3a74a72dcebf21"
    private const val baseUrl = "https://api.flickr.com/services/rest/"
    private val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
    private val photosService = retrofit.create(PhotosService::class.java)

    var pages = 1
        private set

    suspend fun getPhotos(tags: String): List<Photo> {
        val photos = mutableListOf<Photo>()
        var photosJsonObject: JsonObject? = null
        // get photos from the first three pages
        repeat(3) {
            val searchPhotosJsonObject = photosService.getPhotos(tags, it + 1)
            photosJsonObject = searchPhotosJsonObject["photos"]?.asJsonObject
            val photosJsonArray = photosJsonObject?.get("photo")?.asJsonArray
            if (photosJsonArray != null) {
                photos.addAll(Gson().fromJson<List<Photo>>(photosJsonArray, object : TypeToken<List<Photo>>() {}.type))
            }
        }
        pages = photosJsonObject?.get("pages")?.asInt ?: 1
        return photos
    }

    suspend fun getPhotos(tags: String, page: Int): List<Photo> {
        val recentPhotosJsonObject = photosService.getPhotos(tags, page)
        val photosJsonObject = recentPhotosJsonObject["photos"]?.asJsonObject
        val photosJsonArray = photosJsonObject?.get("photo")?.asJsonArray
        return if (photosJsonArray == null) emptyList() else Gson().fromJson(photosJsonArray, object : TypeToken<List<Photo>>() {}.type)
    }
}

private interface PhotosService {
    // using api method flickr.photos.search, also I could use flickr.photos.getRecent to get less photos
    @GET("?method=flickr.photos.search&extras=url_s,url_m,owner_name,date_taken,description&format=json&nojsoncallback=1&api_key=${PhotosData.apiKey}")
    suspend fun getPhotos(@Query("tags") tags: String, @Query("page") page: Int): JsonObject
}
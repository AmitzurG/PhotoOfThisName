package com.example.photosthisname.data

import com.google.gson.annotations.SerializedName

data class Photo(
    @SerializedName("ownername") val ownerName: String,
    @SerializedName("datetaken") val dateTaken: String,
    val description: Description,
    val url_s: String,
    val url_m: String)

data class Description(@SerializedName("_content") val content: String?)

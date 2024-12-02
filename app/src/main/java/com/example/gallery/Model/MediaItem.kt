package com.example.gallery.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class MediaItemModel : Parcelable {
    @Parcelize
    data class Image(val url: String) : MediaItemModel()

    @Parcelize
    data class Video(val url: String) : MediaItemModel()
}

@Parcelize
data class MediaFolder(
    val name: String,
    val mediaItems: List<MediaItemModel>
) : Parcelable
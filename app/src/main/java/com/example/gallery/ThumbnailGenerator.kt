package com.example.gallery.Utils

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.File

object ThumbnailGenerator {
    fun getCachedThumbnail(context: Context, videoPath: String): Bitmap? {
        val file = File(videoPath)
        return if (file.exists()) {
            ThumbnailUtils.createVideoThumbnail(
                videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND
            )
        } else {
            null
        }
    }
}

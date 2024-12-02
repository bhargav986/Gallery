package com.example.gallery.Adapter

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallery.MediaDetail
import com.example.gallery.Model.MediaItemModel
import com.example.gallery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryAdapter(
    private val context: Context,
    private val imageUris: List<String>,
    private val videoUris: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_VIDEO = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < imageUris.size) {
            VIEW_TYPE_IMAGE
        } else {
            VIEW_TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.image_item, parent, false)
                ImageViewHolder(view)
            }
            VIEW_TYPE_VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.video_item, parent, false)
                VideoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> holder.bind(imageUris[position])
            is VideoViewHolder -> holder.bind(videoUris[position - imageUris.size])
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MediaDetail::class.java).apply {
                putStringArrayListExtra("image_uris", ArrayList(imageUris))
                putStringArrayListExtra("video_uris", ArrayList(videoUris))
                putExtra("current_image_index", position)
                putExtra("media_type", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size + videoUris.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageUri: String) {
            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

            itemView.setOnClickListener {
                navigateToMediaDetail(imageUri, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(videoUri: String) {
            // You can load a thumbnail for the video here if needed
            Glide.with(itemView.context)
                .load(R.drawable.black_placeholder)
                .into(imageView)

            itemView.setOnClickListener {
                navigateToMediaDetail(videoUri, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            }
        }
    }

    private fun navigateToMediaDetail(uri: String, mediaType: Int) {
        val intent = Intent(context, MediaDetail::class.java).apply {
            putExtra("media_uri", uri)
            putExtra("media_type", mediaType)
            putStringArrayListExtra("image_uris", ArrayList(imageUris))
            putStringArrayListExtra("video_uris", ArrayList(videoUris))
            putExtra("initial_media_type", mediaType)
        }
        context.startActivity(intent)
    }
}

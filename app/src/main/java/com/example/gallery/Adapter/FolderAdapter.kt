package com.example.gallery.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallery.Model.MediaFolder
import com.example.gallery.Model.MediaItemModel
import com.example.gallery.R

class FolderAdapter(
    private val folders: List<MediaFolder>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        holder.bind(folder)
        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(folder)
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderNameTextView: TextView = itemView.findViewById(R.id.folderNameTextView)
        private val folderImageView: ImageView = itemView.findViewById(R.id.folderImageView)

        fun bind(folder: MediaFolder) {
            folderNameTextView.text = folder.name

            // Load the first image of the folder
            val firstImage = folder.mediaItems.firstOrNull { it is MediaItemModel.Image }
            if (firstImage != null) {
                Glide.with(itemView.context)
                    .load((firstImage as MediaItemModel.Image).url)
                    .into(folderImageView)
            } else {
                // Set a placeholder or default image if the folder doesn't contain any images
                folderImageView.setImageResource(R.drawable.black_placeholder)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(folder: MediaFolder)
    }
}
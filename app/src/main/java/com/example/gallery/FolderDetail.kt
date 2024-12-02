package com.example.gallery

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.Adapter.GalleryAdapter
import com.example.gallery.Model.MediaItemModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FolderDetail : AppCompatActivity() {
    private lateinit var backIcon: ImageView
    private lateinit var folderNameTxt: TextView
    private lateinit var menuIcon: ImageView
    private lateinit var recyclerView: RecyclerView

    private var folderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        )

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        backIcon = findViewById(R.id.backIcon)
        folderNameTxt = findViewById(R.id.folderNameTxt)
        menuIcon = findViewById(R.id.menuIcon)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        folderName = intent.getStringExtra("folder_name")
        if (folderName != null) {
            Log.d("FolderDetail", "Loading images for folder: $folderName")
            loadMediaFromDevice(folderName!!)
        } else {
            Log.e("FolderDetail", "Folder name is null or empty.")
        }

        folderNameTxt.text = folderName

        backIcon.setOnClickListener(){
            finish()
        }

        menuIcon.setOnClickListener(){
            val popup = PopupMenu(this, it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.main_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.newFolder -> {
                        true
                    }

                    R.id.settings -> {
                        true
                    }

                    R.id.trash -> {
                        true
                    }

                    R.id.favourite -> {
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun loadMediaFromDevice(folderName: String) {
        recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val mediaItems = fetchMediaItems(folderName)
            val imageUris = mediaItems.filterIsInstance<MediaItemModel.Image>().map { it.url }
            val videoUris = mediaItems.filterIsInstance<MediaItemModel.Video>().map { it.url }
            recyclerView.adapter = GalleryAdapter(this@FolderDetail, imageUris, videoUris)
            recyclerView.visibility = View.VISIBLE
        }
    }

    private suspend fun fetchMediaItems(folderName: String): MutableList<MediaItemModel> =
        withContext(Dispatchers.IO) {
            val mediaItems: MutableList<MediaItemModel> = mutableListOf()
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATA
            )
            val selection = "${MediaStore.Files.FileColumns.DATA} like ?"
            val selectionArgs = arrayOf("%$folderName%")

            val cursor: Cursor? = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )

            cursor?.use {
                Log.d("FolderDetail", "Cursor count: ${it.count}")
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val mediaTypeColumn =
                    it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val mediaType = it.getInt(mediaTypeColumn)
                    val data = it.getString(dataColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        id
                    )

                    val mediaItem = when (mediaType) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaItemModel.Image(
                            contentUri.toString()
                        )

                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaItemModel.Video(
                            contentUri.toString()
                        )

                        else -> continue
                    }

                    mediaItems.add(mediaItem)
                }
            }

            cursor?.close()
            mediaItems
        }
}

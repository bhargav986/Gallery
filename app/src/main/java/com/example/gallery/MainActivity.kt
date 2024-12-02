package com.example.gallery

import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.Adapter.FolderAdapter
import com.example.gallery.Model.MediaFolder
import com.example.gallery.Model.MediaItemModel
import java.io.File

class MainActivity : AppCompatActivity(), FolderAdapter.OnItemClickListener {
    private lateinit var menuIcon: ImageView
    private lateinit var recyclerView: RecyclerView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadMediaFolders()
        } else {
            // Explain to the user that the feature is unavailable because the features requires a permission that the user has denied.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        )

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        menuIcon = findViewById(R.id.menuIcon)

        menuIcon.setOnClickListener {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    loadMediaFolders()
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                }

                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            loadMediaFolders()
        }
    }

    private fun loadMediaFolders() {
        val folders = fetchMediaFolders()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FolderAdapter(folders, this)
    }

    override fun onItemClick(folder: MediaFolder) {
        val intent = Intent(this, FolderDetail::class.java)
        intent.putExtra("folder_name", folder.name)
        startActivity(intent)
    }

    private fun fetchMediaFolders(): List<MediaFolder> {
        val folders = mutableListOf<MediaFolder>()
        val folderMap = mutableMapOf<String, MutableList<MediaItemModel>>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )

        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            null,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val displayNameColumn =
                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mediaTypeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val displayName = it.getString(displayNameColumn)
                val mediaType = it.getInt(mediaTypeColumn)
                val mimeType = it.getString(mimeTypeColumn)
                val data = it.getString(dataColumn)

                val folderName = File(data).parentFile?.name ?: "Unknown"

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    id
                )

                val mediaItem = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaItemModel.Image(contentUri.toString())
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaItemModel.Video(contentUri.toString())
                    else -> continue
                }

                if (folderMap.containsKey(folderName)) {
                    folderMap[folderName]?.add(mediaItem)
                } else {
                    folderMap[folderName] = mutableListOf(mediaItem)
                }
            }
        }

        folderMap.forEach { (name, mediaItems) ->
            folders.add(MediaFolder(name, mediaItems))
        }

        return folders
    }

}
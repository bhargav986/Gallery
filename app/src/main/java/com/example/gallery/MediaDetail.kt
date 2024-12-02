package com.example.gallery

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.GestureDetector
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.io.File
import java.io.IOError
import java.io.IOException

class MediaDetail : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var backIcon: ImageView
    private lateinit var shareIcon: ImageView
    private lateinit var deleteIcon: ImageView
    private lateinit var menuIcon: ImageView

    private lateinit var imageView: PhotoView
    private lateinit var playerView: PlayerView

    private lateinit var imageUris: List<String>
    private var currentImageIndex: Int = 0

    private lateinit var videoUris: List<String>
    private var currentVideoIndex: Int = 0

    private lateinit var gestureDetector: GestureDetector

    private var mediaType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_detail)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        )

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        backIcon = findViewById(R.id.backIcon)
        shareIcon = findViewById(R.id.shareIcon)
        deleteIcon = findViewById(R.id.deleteIcon)
        menuIcon = findViewById(R.id.menuIcon)

        gestureDetector = GestureDetector(this, GestureListener())

        imageView = findViewById(R.id.imageView)
        playerView = findViewById(R.id.playerView)

        imageUris = intent.getStringArrayListExtra("image_uris") ?: emptyList()
        videoUris = intent.getStringArrayListExtra("video_uris") ?: emptyList()
        currentImageIndex = intent.getIntExtra("current_image_index", 0)

        // Display initial media based on the type (image or video)

        mediaType = intent.getIntExtra("media_type", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            displayImage(currentImageIndex)
        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            displayVideo(currentVideoIndex)
        }

        backIcon.setOnClickListener() {
            finish()
        }

        shareIcon.setOnClickListener() {

        }

        deleteIcon.setOnClickListener() {

        }

        menuIcon.setOnClickListener() {
            val popup = PopupMenu(this, it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.media_detail_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.rotateRight -> {
                        rotateImage(90)
                        true
                    }

                    R.id.rotateLeft -> {
                        rotateImage(-90)
                        true
                    }

                    R.id.rotate180 -> {
                        rotateImage(180)
                        true
                    }

                    R.id.setAs -> {
                        setAsWallpaper()
                        true
                    }

                    R.id.rename -> {
                        renameMedia()
                        true
                    }

                    R.id.hide -> {
                        hideMedia()
                        true
                    }

                    R.id.unhide -> {
                        unhideMedia()
                        true
                    }

                    R.id.copy -> {
                        copyMedia()
                        true
                    }

                    R.id.move -> {
                        moveMedia()
                        true
                    }

                    R.id.openWith -> {
                        openWithExternalApp()
                        true
                    }

                    R.id.createShortcut -> {
                        createShortcut()
                        true
                    }

                    R.id.properties -> {
                        showProperties()
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }

        findViewById<View>(R.id.rootLayout).setOnClickListener {
            onRootLayoutClick(it)
        }

        findViewById<View>(R.id.imageView).setOnClickListener {
            onRootLayoutClick(it)
        }

        findViewById<View>(R.id.playerView).setOnClickListener {
            onRootLayoutClick(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private fun displayImage(index: Int) {
        if (index in imageUris.indices) {
            val uri = imageUris[index]
            imageView.visibility = View.VISIBLE
            playerView.visibility = View.GONE
            Glide.with(this)
                .load(Uri.parse(uri))
                .into(imageView)
        }
    }

    private fun displayVideo(index: Int) {
        if (index in videoUris.indices) {
            val uri = videoUris[index]
            imageView.visibility = View.GONE
            playerView.visibility = View.VISIBLE

            player = ExoPlayer.Builder(this).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
                prepare()
                playWhenReady = true
            }
            playerView.player = player
        }
    }

    private fun navigateToNextImage() {
        currentImageIndex++
        if (currentImageIndex >= imageUris.size) {
            currentImageIndex = 0 // Wrap around to the first image
        }
        displayImage(currentImageIndex)
    }

    private fun navigateToPreviousImage() {
        currentImageIndex--
        if (currentImageIndex < 0) {
            currentImageIndex = imageUris.size - 1 // Wrap around to the last image
        }
        displayImage(currentImageIndex)
    }

    private fun navigateToNextVideo() {
        currentVideoIndex++
        if (currentVideoIndex >= videoUris.size) {
            currentVideoIndex = 0 // Wrap around to the first video
        }
        displayVideo(currentVideoIndex)
    }

    private fun navigateToPreviousVideo() {
        currentVideoIndex--
        if (currentVideoIndex < 0) {
            currentVideoIndex = videoUris.size - 1 // Wrap around to the last video
        }
        displayVideo(currentVideoIndex)
    }

    private fun onRootLayoutClick(view: View) {
        if (backIcon.visibility == View.VISIBLE) {
            backIcon.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    backIcon.visibility = View.GONE
                }
                .start()
        } else {
            backIcon.visibility = View.VISIBLE
            backIcon.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        if (shareIcon.visibility == View.VISIBLE) {
            shareIcon.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    shareIcon.visibility = View.GONE
                }
                .start()
        } else {
            shareIcon.visibility = View.VISIBLE
            shareIcon.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        if (deleteIcon.visibility == View.VISIBLE) {
            deleteIcon.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    deleteIcon.visibility = View.GONE
                }
                .start()
        } else {
            deleteIcon.visibility = View.VISIBLE
            deleteIcon.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        if (menuIcon.visibility == View.VISIBLE) {
            menuIcon.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    menuIcon.visibility = View.GONE
                }
                .start()
        } else {
            menuIcon.visibility = View.VISIBLE
            menuIcon.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            val diffX = e1?.x?.let { e2.x - it } ?: 0f
            val diffY = e1?.y?.let { e2.y - it } ?: 0f

            if (Math.abs(diffX) > Math.abs(diffY) &&
                Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    // Swipe right (previous image)
                    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        navigateToPreviousImage()
                    } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        navigateToPreviousVideo()
                    }
                } else {
                    // Swipe left (next image)
                    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        navigateToNextImage()
                    } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        navigateToNextVideo()
                    }
                }
                return true
            }
            return false
        }
    }

    private fun rotateImage(degrees: Int) {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        imageView.setImageBitmap(rotatedBitmap)
    }

    private fun setAsWallpaper() {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val wallpaperManager = WallpaperManager.getInstance(this)
        try {
            wallpaperManager.setBitmap(bitmap)
            Toast.makeText(this, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renameMedia() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = File(currentUri.path)

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Rename")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(input)

        dialog.setPositiveButton("Rename") { _, _ ->
            val newName = input.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val newFile = File(currentFile.parent, newName)

            if (newFile.exists()) {
                Toast.makeText(this, "File with this name already exists", Toast.LENGTH_SHORT)
                    .show()
                return@setPositiveButton
            }

            try {
                if (currentFile.renameTo(newFile)) {
                    Toast.makeText(this, "File renamed successfully", Toast.LENGTH_SHORT).show()
                    // Update the URI in the list
                    imageUris = imageUris.toMutableList().apply {
                        set(currentImageIndex, newFile.absolutePath)
                    }
                } else {
                    Toast.makeText(this, "Failed to rename file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun hideMedia() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = File(currentUri.path!!)
        val hiddenFolder = File(currentFile.parent, ".hidden")
        if (!hiddenFolder.exists()) hiddenFolder.mkdir()
        val hiddenFile = File(hiddenFolder, currentFile.name)
        if (currentFile.renameTo(hiddenFile)) {
            Toast.makeText(this, "File hidden successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to hide file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unhideMedia() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = File(currentUri.path!!)
        val parentFolder =
            currentFile.parentFile!!.parentFile // Move up two levels to get out of the .hidden folder
        val unhiddenFile = File(parentFolder, currentFile.name)
        if (currentFile.renameTo(unhiddenFile)) {
            Toast.makeText(this, "File unhidden successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to unhide file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyMedia() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = File(currentUri.path.toString())
        val destFile = File(currentFile.parent, "copy_${currentFile.name}")
        try {
            currentFile.copyTo(destFile, overwrite = true)
            Toast.makeText(this, "File copied successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveMedia() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = currentUri.path?.let { File(it) }
        val destFile = File(currentFile?.parent, "moved_${currentFile?.name}")
        if (currentFile?.renameTo(destFile) == true) {
            Toast.makeText(this, "File moved successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to move file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWithExternalApp() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(currentUri, contentResolver.getType(currentUri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(intent, "Open with"))
    }

    private fun createShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use ShortcutManager for API level 26 and above
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val shortcutInfo = ShortcutInfo.Builder(this, "shortcut_id")
                    .setShortLabel("Shortcut Name")
                    .setLongLabel("Long Shortcut Name")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_share))
//                    .setIntent(Intent(this, YourTargetActivity::class.java).apply {
//                        action = Intent.ACTION_VIEW
//                    })
                    .build()
                val pinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(shortcutInfo)
                val successCallback =
                    PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent, 0)
                shortcutManager.requestPinShortcut(shortcutInfo, successCallback.intentSender)
            }
        } else {
            // Use ShortcutManagerCompat for API level 25 and below
//            val shortcutIntent = Intent(this, YourTargetActivity::class.java).apply {
//                action = Intent.ACTION_VIEW
//            }
            val shortcut = ShortcutInfoCompat.Builder(this, "shortcut_id")
                .setShortLabel("Shortcut Name")
                .setLongLabel("Long Shortcut Name")
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_share))
//                .setIntent(shortcutIntent)
                .build()
            ShortcutManagerCompat.requestPinShortcut(this, shortcut, null)
        }
    }

    private fun showProperties() {
        val currentUri = Uri.parse(imageUris[currentImageIndex]) // or videoUris[currentVideoIndex]
        val currentFile = File(currentUri.path)
        val properties = StringBuilder().apply {
            append("Name: ${currentFile.name}\n")
            append("Path: ${currentFile.absolutePath}\n")
            append("Size: ${currentFile.length()} bytes\n")
            // Add more properties if needed
        }.toString()
        AlertDialog.Builder(this)
            .setTitle("Properties")
            .setMessage(properties)
            .setPositiveButton("OK", null)
            .show()
    }
}
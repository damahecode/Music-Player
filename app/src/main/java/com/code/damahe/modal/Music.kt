package com.code.damahe.modal

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val path: String,
    val duration: Long,
    val artUri: Uri,
    val trackUri: Uri
)

@SuppressLint("Recycle", "Range")
fun getAudioList(context: Context): ArrayList<Music> {
    val tempList = ArrayList<Music>()

    val id = if (Build.VERSION.SDK_INT in 24..32) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_AUDIO
    if (context.checkSelfPermission(id) != PackageManager.PERMISSION_GRANTED)
        return tempList

    val sortingList = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATE_ADDED + " DESC",
        MediaStore.Audio.Media.SIZE + " DESC")
    val sortOrder = 0
    val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"
    val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
    val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,selection,null,
        sortingList[sortOrder], null)
    if(cursor != null) {
        if(cursor.moveToFirst()) {
            do {
                val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: "Unknown"
                val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: "Unknown"
                val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: "Unknown"
                val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val uriAlbumArt = Uri.parse("content://media/external/audio/albumart")
                val artUri = ContentUris.withAppendedId(uriAlbumArt, albumIdC)
                val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idC.toLong())
                val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, path = pathC, duration = durationC, artUri = artUri, trackUri = trackUri)
//                val file = File(music.path)
//                if(file.exists())
//                    tempList.add(music)
                tempList.add(music)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
    return tempList
}

fun getImgArt(context: Context, trackUri: Uri?): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, trackUri)
    return retriever.embeddedPicture
}

fun getImage(imgArt: ByteArray?): Bitmap? {
    return try {
        BitmapFactory.decodeByteArray(imgArt, 0, imgArt?.size!!)
    } catch (e: Exception) {
        null  //BitmapFactory.decodeResource(context.resources, R.drawable.music_note_24)
    }
}

fun getDominantColor(bitmap: Bitmap?): Int {
    if (bitmap == null) return Color.CYAN
    val newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
    val color = newBitmap.getPixel(0, 0)
    newBitmap.recycle()
    return color
}
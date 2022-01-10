package com.example.appcompatscopestorage.`interface`

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.UriUtils
import com.example.appcompatscopestorage.request.FileRequestBean
import com.example.appcompatscopestorage.response.FileResponseBean
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Description:
 * Created by WuQuan on 2022/1/7.
 */
class AndroidQFileManager : IFile {

    companion object {
        @JvmStatic
        val instance by lazy {
            AndroidQFileManager()
        }
        const val OPEN_FILE_DESCRIPTOR_MODE_READ = "r"
        const val OPEN_FILE_DESCRIPTOR_MODE_WRITE = "w"
    }


    /**
     * bitmap默认保存在
     * picture/应用包名下，
     * 音频保存在music/应用包名下，
     * 视频保存在movies/应用包名下，
     * 其他download/应用包名下
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun createFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        val filePath = fileRequestBean.destPath
        val pair = getContentValues(filePath)
        val mediaType = pair.first
        val contentValues = pair.second

        val contentUri = when (mediaType) {
            FileTypeNum.IMAGE.number -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            FileTypeNum.AUDIO.number -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            FileTypeNum.VIDEO.number -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        return if (mediaType == FileTypeNum.IMAGE.number) {
            saveBitmap(context, contentValues, contentUri, fileRequestBean)
        } else {
            saveFile(context, contentValues, contentUri, fileRequestBean)
        }
    }

    override fun deleteFile(context: Context, path: String): Boolean {
        if (path.isNotEmpty()) {
            return context.contentResolver.delete(UriUtils.file2Uri(File(path)), null, null) >= 0
        }
        return false
    }

    override fun deleteFile(context: Context, uri: Uri?): Boolean {
        if (uri != null) {
            if (isFileExists(context, uri)) {
                return context.contentResolver.delete(uri, null, null) >= 0
            }
        }
        return false
    }

    override fun deleteFile(context: Context, uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    override fun isFileExists(context: Context, uri: Uri): Boolean {
        var isExists = false
        try {
            context.contentResolver.openFileDescriptor(uri, OPEN_FILE_DESCRIPTOR_MODE_READ)?.use {
                isExists = it.fileDescriptor.valid()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return isExists
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFile(context: Context, contentValues: ContentValues, url: Uri, fileRequestBean: FileRequestBean): FileResponseBean {
        val fileResponseBean = FileResponseBean()
        context.contentResolver?.let { resolver ->
            resolver.insert(url, contentValues)?.let { insertUrl ->
                fileResponseBean.uri = insertUrl
                fileResponseBean.isSuccess = true
                resolver.openOutputStream(insertUrl)?.use { outPut ->
                    val fileInput = FileInputStream(fileRequestBean.srcPath)
                    var read: Int = -1
                    val buffer = ByteArray(2048)
                    while (fileInput.read(buffer).also { read = it } != -1) {
                        outPut.write(buffer, 0, read)
                    }
                }
            }
        }
        return fileResponseBean
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmap(context: Context, contentValues: ContentValues, url: Uri, fileRequestBean: FileRequestBean): FileResponseBean {
        val fileResponseBean = FileResponseBean()
        context.contentResolver?.let { resolver ->
            resolver.insert(url, contentValues)?.let { insertUrl ->
                fileResponseBean.uri = insertUrl
                fileResponseBean.isSuccess = true
                resolver.openOutputStream(insertUrl)?.use {
                    val bitmap = BitmapFactory.decodeFile(fileRequestBean.srcPath)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
        return fileResponseBean
    }


    override fun queryFilePathByUri(context: Context, uri: Uri): String? {
        if (isFileExists(context, uri)) return UriUtils.uri2File(uri).absolutePath
        return ""
    }

    override fun queryAllMediaData(context: Context, queryUri: Uri, idKey: String): List<Uri> {
        val selection = null
        // 查询条件的具体值。规范的查询，应该是在selection中写条件。例如name = ?, id = ?.
        val args = arrayOf<String>()
        val projection = arrayOf<String>()
        val sort = MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc"
        val result = mutableListOf<Uri>()
        context.contentResolver?.query(queryUri, projection, selection, args, sort)?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndex(idKey)
                if (idIndex == -1) {
                    continue
                }
                val id = it.getLong(idIndex)
                ContentUris.withAppendedId(queryUri, id).let { uri ->
                    result.add(uri)
                }
            }
        }
        return result
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getContentValues(filePath: String): Pair<Int, ContentValues> {
        var mediaType = 0
        val contentValues = if (filePath.endsWith(".jpg") || filePath.endsWith(".png") || filePath.endsWith(".gif")) {
            mediaType = FileTypeNum.IMAGE.number
            buildPictureContentValues(filePath)
        } else if (filePath.endsWith(".mov") || filePath.endsWith(".wmv") || filePath.endsWith(".mp4") || filePath.endsWith(".mkv") || filePath.endsWith(".h264") || filePath.endsWith(".h265")) {
            mediaType = FileTypeNum.VIDEO.number
            buildVideoContentValues(filePath)
        } else if (filePath.endsWith(".mp3") || filePath.endsWith(".aac")) {
            mediaType = FileTypeNum.AUDIO.number
            buildAudioContentValues(filePath)
        } else {
            mediaType = FileTypeNum.OTHER.number
            buildDownLoadContentValues(filePath)
        }
        return Pair(mediaType, contentValues)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildPictureContentValues(filePath: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_image")
            put(MediaStore.Images.Media.DISPLAY_NAME, getCurrentTime() + "_" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length))
            put(MediaStore.Images.Media.MIME_TYPE, "image/${filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length)}")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/${AppUtils.getAppPackageName()}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildVideoContentValues(filePath: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_movie")
            put(MediaStore.Images.Media.DISPLAY_NAME, getCurrentTime() + "_" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length))
            put(MediaStore.Images.Media.MIME_TYPE, "video/${filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length)}")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/${AppUtils.getAppPackageName()}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildAudioContentValues(filePath: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_audio")
            put(MediaStore.Images.Media.DISPLAY_NAME, getCurrentTime() + "_" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length))
            put(MediaStore.Images.Media.MIME_TYPE, "audio/${filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length)}")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/${AppUtils.getAppPackageName()}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildDownLoadContentValues(filePath: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_video")
            put(MediaStore.Images.Media.DISPLAY_NAME, getCurrentTime() + "_" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length))
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/${AppUtils.getAppPackageName()}")
        }
    }


    private fun getCurrentTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    }
}
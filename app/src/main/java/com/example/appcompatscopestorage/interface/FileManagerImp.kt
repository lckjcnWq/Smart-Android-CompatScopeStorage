package com.example.appcompatscopestorage.`interface`

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
import com.example.appcompatscopestorage.request.FileRequestBean
import com.example.appcompatscopestorage.response.FileResponseBean
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Description:
 * Created by WuQuan on 2022/1/7.
 */
class FileManagerImp : IFile {

    companion object {
        @JvmStatic
        val instance by lazy {
            FileManagerImp()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun createFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean{
        val filePath = fileRequestBean.destPath
        val pair = getContentValues(filePath)
        val mediaType = pair.first
        val contentValues = pair.second

        val contentUri= when (mediaType) {
            FileTypeNum.IMAGE.number -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            FileTypeNum.AUDIO.number -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            FileTypeNum.VIDEO.number -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        return if (mediaType == FileTypeNum.IMAGE.number) {
            saveBitmap(context,contentValues,contentUri,fileRequestBean)
        }else{
            saveFile(context,contentValues,contentUri,fileRequestBean)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFile(context: Context, contentValues: ContentValues, url: Uri, fileRequestBean: FileRequestBean) :FileResponseBean{
        val fileResponseBean = FileResponseBean()
        context.contentResolver?.let { resolver ->
            resolver.insert(url,contentValues)?.let { insertUrl ->
                fileResponseBean.url=insertUrl
                fileResponseBean.isSuccess=true
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
    private fun saveBitmap(context: Context, contentValues: ContentValues, url: Uri, fileRequestBean: FileRequestBean) :FileResponseBean{
        val fileResponseBean = FileResponseBean()
        context.contentResolver?.let { resolver ->
            resolver.insert(url,contentValues)?.let { insertUrl ->
                fileResponseBean.url=insertUrl
                fileResponseBean.isSuccess=true
                resolver.openOutputStream(insertUrl)?.use {
                    val bitmap=BitmapFactory.decodeFile(fileRequestBean.srcPath)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
        return fileResponseBean
    }


    override fun deleteFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
       return FileResponseBean()
    }

    override fun updateFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        return FileResponseBean()
    }

    override fun queryFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        return FileResponseBean()
    }

    override fun renameFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        return FileResponseBean()
    }

    override fun moveFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        return FileResponseBean()
    }

    override fun copyFile(context: Context, fileRequestBean: FileRequestBean): FileResponseBean {
        return FileResponseBean()
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
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/${AppUtils.getAppPackageName()}")
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
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/${AppUtils.getAppPackageName()}")
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
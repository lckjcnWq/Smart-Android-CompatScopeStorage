package com.example.appcompatscopestorage.`interface`

import android.content.Context
import android.net.Uri
import com.example.appcompatscopestorage.request.FileRequestBean
import com.example.appcompatscopestorage.response.FileResponseBean

/**
 * Description:
 * Created by WuQuan on 2022/1/7.
 */
interface IFile {

   fun createFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun deleteFile(context: Context,path:String):Boolean

   fun deleteFile(context: Context,uri: Uri?):Boolean

   fun deleteFile(context: Context,uris:List<Uri>)

   fun isFileExists(context: Context,uri: Uri):Boolean

   fun queryFilePathByUri(context: Context,uri: Uri): String?

   fun queryAllMediaData(context: Context, queryUri: Uri, idKey: String): List<Uri>
}
package com.example.appcompatscopestorage.`interface`

import android.content.Context
import com.example.appcompatscopestorage.request.FileRequestBean
import com.example.appcompatscopestorage.response.FileResponseBean

/**
 * Description:
 * Created by WuQuan on 2022/1/7.
 */
interface IFile {

   fun createFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun deleteFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun updateFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun queryFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun renameFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun moveFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean

   fun copyFile(context: Context,fileRequestBean: FileRequestBean):FileResponseBean
}
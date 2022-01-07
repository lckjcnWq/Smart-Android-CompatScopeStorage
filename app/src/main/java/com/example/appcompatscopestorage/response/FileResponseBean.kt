package com.example.appcompatscopestorage.response

import android.net.Uri

/**
 * Description:
 * Created by WuQuan on 2022/1/7.
 */
data class FileResponseBean(
        var url: Uri? =null,
        var isSuccess:Boolean=false
)
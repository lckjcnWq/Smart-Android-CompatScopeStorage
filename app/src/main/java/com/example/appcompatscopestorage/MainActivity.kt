package com.example.appcompatscopestorage

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.example.appcompatscopestorage.`interface`.FileManagerImp
import com.example.appcompatscopestorage.request.FileRequestBean

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionUtils.permissionGroup(PermissionConstants.STORAGE)
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                }

                override fun onDenied(permissionsDeniedForever: List<String>,
                                      permissionsDenied: List<String>) {
                }
            })
            .request()

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Thread{
                val result=FileManagerImp.instance.createFile(this, FileRequestBean("/sdcard/Android/data/com.example.appcompatscopestorage/files/example_equi.jpg","test.png"))
                val result1=FileManagerImp.instance.createFile(this, FileRequestBean("/sdcard/Android/data/com.example.appcompatscopestorage/files/f.txt","test.txt"))
                val result2=FileManagerImp.instance.createFile(this, FileRequestBean("/sdcard/Android/data/com.example.appcompatscopestorage/files/refocus.mp4","test.mp4"))
                LogUtils.i("result= $result")
                LogUtils.i("result1= $result1")
                LogUtils.i("result2= $result2")
            }.start()
        }
    }
}
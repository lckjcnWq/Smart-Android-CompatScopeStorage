package com.example.appcompatscopestorage

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.example.appcompatscopestorage.`interface`.FileManagerImp
import com.example.appcompatscopestorage.request.FileRequestBean
import com.example.appcompatscopestorage.response.FileResponseBean

class MainActivity : AppCompatActivity() {
    private lateinit var tvContent: TextView
    private var result: FileResponseBean?=null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnAdd=findViewById<Button>(R.id.btnAdd)
        val btnDelete=findViewById<Button>(R.id.btnDelete)
        val btnQuery=findViewById<Button>(R.id.btnQuery)
        tvContent=findViewById<TextView>(R.id.tvContent)
        PermissionUtils.permissionGroup(PermissionConstants.STORAGE)
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                }

                override fun onDenied(permissionsDeniedForever: List<String>,
                                      permissionsDenied: List<String>) {
                }
            })
            .request()

        btnAdd.setOnClickListener {
            Thread{
                result=FileManagerImp.instance.createFile(this, FileRequestBean("/sdcard/Android/data/com.example.appcompatscopestorage/files/example_equi.jpg","test.png"))
                setContent("当前uri= ${result?.uri}  ,插入是否成功:${result?.isSuccess} ")
            }.start()
        }

        btnDelete.setOnClickListener {
            val isSuccess=FileManagerImp.instance.deleteFile(this,result?.uri)
            setContent("删除是否成功:$isSuccess")
        }


        btnQuery.setOnClickListener {
            val path=FileManagerImp.instance.queryFilePathByUri(this,result?.uri!!)
            setContent("当前uri的路径= $path")
        }
    }

    private fun  setContent(content:String){
        runOnUiThread {
            tvContent.text=content
        }
    }
}
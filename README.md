# CompatScopeStorage
AndroidQ  File Storage
可以在公共区进行文件操作的工具类


## :-: **在公共区进行文件操作的工具类**

### **一.依赖**
~~~
mavenCentral()

implementation 'io.github.lckjcnWq:qstorage:1.0.0'

~~~

### **二.使用**
~~~
1.创建文件
兼容图片/音频/视频/txt等文件
AndroidQFileManager.getInstance().createFile(this, new FileRequestBean(String srcPath,String destPath))

2.删除文件
AndroidQFileManager.getInstance().deleteFile(this,uri)

3.查询文件
AndroidQFileManager.getInstance().queryFilePathByUri(uri)
~~~





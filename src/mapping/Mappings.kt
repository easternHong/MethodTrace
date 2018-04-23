package mapping

import okhttp3.OkHttpClient
import okhttp3.Request
import revert.FieldDesc
import revert.MethodDesc
import revert.RawClass
import utils.*
import utils.apk.ApkUtils
import java.io.*
import java.util.*
import kotlin.collections.set


/**
 * 对应插件mappings文件处理，提取mappings.txt文件中的class
 */
class Mappings : IMapping {
    var mConfig: IConfig = ConfigImpl()


    private fun preConfig() {
        val apkDesc = mConfig.mApkDesc
        mConfig.traceFilePath = apkDesc.traceFile
        mConfig.skipDownloadMapping["com.yy.mobile.qupaishenqu"] = true
        mConfig.workingDir = "../tmp"
        mConfig.buildBranch = apkDesc.branchName
        if (TextUtils.isEmpty(mConfig.aAptFile)) {
            mConfig.aAptFile = "/home/g8489/Android/sdk/build-tools/26.0.2/aapt"
        }
        val tmpConfig = ApkUtils.getVersionCode(mConfig.aAptFile, apkDesc.apkFile)
        mConfig.mBuiltInPluginVersionCode["host"] = tmpConfig.hostVersion
        mConfig.mBuiltInPluginVersionCode.putAll(tmpConfig.mBuiltInPluginVersionCode)
    }

    private fun downloadMappings() {
        val time = System.currentTimeMillis()
        if (TextUtils.isEmpty(mConfig.buildBranch)) {
            throw IllegalArgumentException("分支版本号还没指定")
        }
        try {
            mConfig.mBuiltInPluginRepo["host"] = "http://repo.yypm.com/dwbuild/mobile/android/entmobile/entmobile-%s/"
            for (pluginName in mConfig.mBuiltInPluginRepo.keys) {
                if (mConfig.skipDownloadMapping[pluginName] != null) {
                    println("使用本地的mapping文件:$pluginName")
                    continue
                }
                val client = OkHttpClient.Builder()
                        .build()
                val httpUrl = String.format(mConfig.mBuiltInPluginRepo[pluginName]!!, mConfig.buildBranch)
                val call = client.newCall(Request.Builder()
                        .url(httpUrl)
                        .build())
                val response = call.execute().body().string().toString()
                val vCode = mConfig.mBuiltInPluginVersionCode[pluginName].toString()
                val vCodeIndex = response.indexOf("-".plus(vCode).plus("-r"))
                if (vCodeIndex == -1) {
                    println("找不到{$pluginName}:".plus(vCode))
                } else {
                    //find first
                    var c = ""
                    var left = vCodeIndex
                    while (c != "\"") {
                        c = response.substring(left - 1, left)
                        left--
                    }
                    var right = vCodeIndex
                    //find last
                    while (c != "/") {
                        c = response.substring(right, right + 1)
                        right++
                    }
                    val version = response.subSequence(left + 1, right - 1)
                    val mapping = if (pluginName == "host") {
                        String.format(mConfig.mBuiltInPluginRepo[pluginName]!!, mConfig.buildBranch)
                                .plus(version).plus(File.separator).plus("proguard/mapping.txt")
                    } else {
                        String.format(mConfig.mBuiltInPluginRepo[pluginName]!!, mConfig.buildBranch)
                                .plus(version).plus(File.separator).plus("mapping.txt")
                    }
                    println("$pluginName:$mapping")
                    println("开始$pluginName-mapping")
                    DownloadUtil.get().download(mapping, mConfig.workingDir, pluginName.plus("-").plus(vCode).plus("-"), object : DownloadUtil.OnDownloadListener {
                        override fun onDownloadFailed(e: Exception) {
                            println(e.toString())
                        }

                        override fun onDownloadSuccess() {

                        }

                        override fun onDownloading(progress: Int) {
//                            print(progress)
                        }
                    })
                }
            }
        } finally {
            println(Constant.LOGGER_TAG.plus("mapping链接获取：").plus(System.currentTimeMillis() - time).plus("ms"))
        }
    }

    override fun collectMapping(config: IConfig): Map<String, RawClass> {
        this.mConfig = config
        preConfig()
        downloadMappings()
        val time = System.currentTimeMillis()
        val map = format(config)
        Log.d(Constant.LOGGER_TAG, "parse map 花费时间:".plus(System.currentTimeMillis() - time).plus("ms"))
        return map
    }

    private fun format(config: IConfig): Map<String, RawClass> {
        val map = HashMap<String, RawClass>()
        val fileList = config.getMappings()
        for (path in fileList) {
            map.putAll(collectClassMap(path))
        }
        return map
    }

    private fun collectClassMap(filePath: String): Map<String, RawClass> {
        val file = File(filePath)
        val fileContent = HashMap<String, RawClass>()
        if (!file.isFile) {
            return fileContent
        }
        var reader: BufferedReader? = null
        try {
            val `is` = InputStreamReader(FileInputStream(file), Charsets.UTF_8.name())
            reader = BufferedReader(`is`)
            var clazz = RawClass()
            var clazzName = ""
            var start = true
            while (true) {
                val line = reader.readLine() ?: break
                if (!TextUtils.isEmpty(clazzName) && line.startsWith(" ") && start) {
                    //field or method
                    if (line.contains("(")) {
                        //method
                        clazz.mList.add(formatMethod(line))
                    } else {
                        //field
                        clazz.fList.add(formatField(line))
                    }
                } else {
                    //pkg
                    val pair = extractPackageName(line)
                    if (!TextUtils.isEmpty(pair.first)) {
                        clazz = RawClass()
                        clazz.name = pair.first
                        clazz.pName = pair.second
                        clazzName = pair.first
                        start = true
                        fileContent[clazz.name] = clazz
                    } else {
                        start = false
                    }
                }
            }
            reader.close()
            return fileContent
        } catch (e: IOException) {
            throw RuntimeException("IOException occurred. ", e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    throw RuntimeException("IOException occurred. ", e)
                }

            }
        }

    }

    private fun formatField(line: String): FieldDesc {
        val array = line.trim().split(" ")
        val fieldDesc = FieldDesc()
        fieldDesc.type = array[0]
        fieldDesc.name = array[1]
        fieldDesc.pName = array[3]
        return fieldDesc
    }

    private fun formatMethod(line: String): MethodDesc {
        val methodDesc = MethodDesc()
        val array = line.trim().split(" ")
        try {
            methodDesc.rType = array[0].substring(array[0].lastIndexOf(":") + 1)
            methodDesc.name = array[1]
            methodDesc.pName = array[3]
        } catch (e: Exception) {
            println("错了:${(array)}")
        }
        return methodDesc
    }

    private fun extractPackageName(content: String): Pair<String, String> {
        if (content.startsWith(" ")) return Pair("", "")
        val index = content.indexOf("->")
        if (index > 0) {
            val rawPkg = content.substring(0, index).replace(" ", "")
            val proPkg = content.substring(index + 2).replace(" ", "")
            return Pair(rawPkg, proPkg.replace(":", ""))
        }
        return Pair("", "")
    }

    companion object {
        val mappings = Mappings()
    }

}
package mapping

import okhttp3.OkHttpClient
import okhttp3.Request
import revert.FieldDesc
import revert.MethodDesc
import revert.RawClass
import utils.*
import utils.apk.ApkUtils
import java.io.*
import kotlin.collections.set


/**
 * 对应插件mappings文件处理，提取mappings.txt文件中的class
 */
class Mappings : IMapping {

    override fun getNewClass(): Map<String, RawClass> {
        return HashMap()
    }

    override fun collectVersions() {
        val apkDesc = mConfig.mApkDesc
        mConfig.traceFilePath = apkDesc.traceFile
        mConfig.skipDownloadMapping["com.yy.mobile.qupaishenqu"] = true
        val apkFile = File(mConfig.mApkDesc.apkFile)
        mConfig.buildBranch = apkDesc.branchName
        if (TextUtils.isEmpty(mConfig.aAptFile)) {
            mConfig.aAptFile = "/home/g8489/Android/sdk/build-tools/26.0.2/aapt"
        }
        val tmpConfig = ApkUtils.getVersionCode(mConfig.aAptFile, apkDesc.apkFile)
        mConfig.mBuiltInPluginVersionCode["host"] = tmpConfig.hostVersion
        mConfig.hostVersion = tmpConfig.hostVersion
        mConfig.mBuiltInPluginVersionCode.putAll(tmpConfig.mBuiltInPluginVersionCode)
        mConfig.workingDir = apkFile.parent.plus(File.separator).plus(mConfig.hostVersion).plus(File.separator)
        if (!File(mConfig.workingDir).exists()) {
            File(mConfig.workingDir).mkdirs()
        }
    }

    var mConfig: IConfig = ConfigImpl()

    override fun getMappingFileList(): ArrayList<String> {

        //repo copy
        val repoMap = HashMap(mConfig.mBuiltInPluginRepo)
        val oldList = mConfig.getMappings()
        for (i in oldList) {
            val f = File(i)
            if (f.exists()) {
                f.delete()
            }
        }
        downloadMappings(object : DownloadUtil.OnDownloadListener {
            override fun onDownloading(url: String?, progress: Int) {

            }

            override fun onDownloadFailed(url: String?, e: java.lang.Exception?) {
                val pluginName = getPluginNameByUrl(repoMap, url!!)
                println("下载失败:$pluginName")
            }

            override fun onDownloadSuccess(url: String?) {
                val pluginName = getPluginNameByUrl(repoMap, url!!)
                println("下载成功:$pluginName")
            }
        })
        return ArrayList()
    }

    fun getPluginNameByUrl(map: HashMap<String, String>, url: String): String {
        val set = map.keys
        for (p in set) {
            val repo = map[p]!!.replace("%s/", "")
            if (url.startsWith(repo)) {
                return p
            }
        }
        return "entmobile"
    }


    private fun downloadMappings(listener: DownloadUtil.OnDownloadListener) {
        if (TextUtils.isEmpty(mConfig.buildBranch)) {
            throw IllegalArgumentException("分支版本号还没指定")
        }
        mConfig.mBuiltInPluginRepo["host"] = "http://repo.yypm.com/dwbuild/mobile/android/entmobile/entmobile-%s/"

        for (pluginName in mConfig.mBuiltInPluginRepo.keys) {
            if (mConfig.getBuiltInPluginVersionCode().containsKey(pluginName)) {
                downloadSingleMap(pluginName, mConfig.workingDir, listener)
            }
        }
    }

    private fun downloadSingleMap(pluginName: String, destPath: String, listener: DownloadUtil.OnDownloadListener) {
        if (mConfig.skipDownloadMapping[pluginName] != null) {
            println("使用本地的mapping文件:$pluginName")
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
            val mappingUrl = if (pluginName == "host") {
                String.format(mConfig.mBuiltInPluginRepo[pluginName]!!, mConfig.buildBranch)
                        .plus(version).plus(File.separator).plus("proguard/mapping.txt")
            } else {
                String.format(mConfig.mBuiltInPluginRepo[pluginName]!!, mConfig.buildBranch)
                        .plus(version).plus(File.separator).plus("mapping.txt")
            }
            DownloadUtil.get().download(mappingUrl, destPath, pluginName.plus("-").plus(vCode).plus("-"), listener)
        }
    }

    override fun collectMapping(): Map<String, RawClass> {
        val time = System.currentTimeMillis()
        val map = format()
        Log.d(Constant.LOGGER_TAG, "parse map 花费时间:".plus(System.currentTimeMillis() - time).plus("ms"))
        return map
    }

    private fun format(): Map<String, RawClass> {
        val map = HashMap<String, RawClass>()
        val fileList = mConfig.getMappings()
        for (path in fileList) {
            map.putAll(collectClassMap(path))
        }
        return map
    }

    @Synchronized
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
            var lineCnt = 0
            while (true) {
                val line = reader.readLine() ?: break
                lineCnt++
                try {
                    if (!TextUtils.isEmpty(clazzName) && line.startsWith(" ") && start) {
                        //field or method
//                        println()
                        if (line.contains("(")) {
                            //method
                            val m = formatMethod(line, clazz.dartsInner)
                            if (m != null && !m.name.isEmpty()) {
                                clazz.mList.add(m)
                            }
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
                            clazz.dartsInner = pair.third
                            clazzName = pair.first
                            start = true
                            fileContent[clazz.name] = clazz
                        } else {
                            start = false
                        }
                    }
                } catch (e: Exception) {
                    println("出错了:$line,$filePath,$lineCnt,$e")
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
        try {
            fieldDesc.type = array[0]
            fieldDesc.name = array[1]
            fieldDesc.pName = array[3]
        } catch (e: IndexOutOfBoundsException) {
            println("错le0:$line,$e")
            throw IndexOutOfBoundsException(e.localizedMessage)
        }
        return fieldDesc
    }

    private fun formatMethod(line: String, darts: Boolean): MethodDesc {
        val methodDesc = MethodDesc()
        val array = line.trim().split(" ")
        try {
            if (darts && line.contains("Impl access\$")) {
                methodDesc.name = "access"
                methodDesc.pName = "access"
            }
            methodDesc.rType = array[0].substring(array[0].lastIndexOf(":") + 1)

        } catch (e: IndexOutOfBoundsException) {
            println("错le:$line,$e")
            throw IndexOutOfBoundsException(e.localizedMessage)
        }
        return methodDesc
    }

    private fun extractPackageName(content: String): Triple<String, String, Boolean> {
        if (content.startsWith(" ")) return Triple("", "", false)
        val index = content.indexOf("->")
        if (index > 0) {
            val rawPkg = handleDart(content.substring(0, index).replace(" ", ""))
            val proPkg = handleDart(content.substring(index + 2).replace(" ", ""))
            return Triple(rawPkg.first, proPkg.first.replace(":", ""), rawPkg.second)
        }
        return Triple("", "", false)
    }

    val REGEX = "\$\$\$DartsFactory\$\$\$"

    private fun handleDart(content: String): Pair<String, Boolean> {
        //com.yy.android.sniper.apt.darts.yymobile_core$$$DartsFactory$$$8986d5ab57efc76ddbf1ea6237319abc$FreeDataServiceImplDartsInnerInstance
        //com.yy.android.sniper.apt.darts.yymobile_8986d5ab57efc76ddbf1ea6237319abc$FreeDataServiceImplDartsInnerInstance
        if (content.contains(REGEX)) {
            val startIndex = content.indexOf(REGEX)
            return Pair(content.substring(0, startIndex)
                    .plus("DartsFactory")
                    .plus(content.substring(startIndex + REGEX.length + 32)).replace("$", "")
                    .replace(":", ""), true)
        }
        return Pair(content, false)
    }
}
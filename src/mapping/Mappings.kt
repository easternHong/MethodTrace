package mapping

import okhttp3.OkHttpClient
import okhttp3.Request
import utils.*
import utils.apk.ApkUtils
import java.io.*
import kotlin.collections.set


/**
 * 对应插件mappings文件处理，提取mappings.txt文件中的class
 */
class Mappings : IMapping {
    var mConfig: IConfig? = null


    //    private fun downApkFile() {
//        mConfig?.hostVersion = "59724"
//
//        try {
//
//             File apkFile =  File "/home/g8489/diff/yymobile_client-7.7.0-SNAPSHOT-59724-official.apk";
//            if (!apkFile.exists()) {
//                throw  IllegalArgumentException "apk路径不对";
//            }
//            System.out.println("开始解压apk文件");
//            UnzipFiles.unpackZip(apkFile.getParent() + "/", apkFile.getName());
//            System.out.println("解压apk文件结束");
//            File[] files =  File(apkFile.getParent() + "/lib/armeabi-v7a").listFiles(file, s) -> {
//                final String pkgName = s.replace("lib", "")
//                        .replace("_", ".")
//                        .replace(".so", "").replace(" ", "");
//                return config.getBuiltInPlugins().containsKey(pkgName);
//            });
//            for (File f : files) {
//                ApkInfo apkInfo =  ApkUtils().getApkInfo("", f.getAbsolutePath());
//                System.out.println(apkInfo.getPackageName() + ",versionCode:" + apkInfo.getVersionCode());
//                config.getBuiltInPluginVersionCode().put(apkInfo.getPackageName(), apkInfo.getVersionCode());
//            }
//            return config;
//        } catch ( e: Exception) {
//            throw  IllegalArgumentException ("出错了:$e")
//        }
//    }
    private fun preConfig() {
        mConfig?.buildBranch = "android_7.7.0_dior_feature"
        mConfig?.aAptFile = "/home/g8489/Android/sdk/build-tools/26.0.2/aapt"
        val apkConfig = ApkUtils.getVersionCode(mConfig?.aAptFile,
                "/home/g8489/diff/yymobile_client-7.7.0-SNAPSHOT-59724-official.apk")
        mConfig?.mBuiltInPluginVersionCode?.putAll(apkConfig.mBuiltInPluginVersionCode)
    }

    private fun downloadMappings() {
        val time = System.currentTimeMillis()
        if (TextUtils.isEmpty(mConfig?.buildBranch)) {
            throw IllegalArgumentException("分支版本号还没指定")
        }
        try {
            if (mConfig?.skipDownloadMapping!!) {
                println("使用本地的mapping文件")
                return
            }
            mConfig!!.mBuiltInPluginRepo["host"] = "http://repo.yypm.com/dwbuild/mobile/android/entmobile/entmobile-%s/"
            mConfig!!.mBuiltInPluginVersionCode["host"] = "59724"
            for (pluginName in mConfig!!.mBuiltInPluginRepo.keys) {
                val client = OkHttpClient.Builder()
                        .build()
                val httpUrl = String.format(mConfig!!.mBuiltInPluginRepo[pluginName]!!, mConfig?.buildBranch)
                val call = client.newCall(Request.Builder()
                        .url(httpUrl)
                        .build())
                val response = call.execute().body().string().toString()
                val vCode = mConfig?.mBuiltInPluginVersionCode!![pluginName].toString()
                val vCodeIndex = response.indexOf(vCode.plus("-r"))
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
                        String.format(mConfig?.mBuiltInPluginRepo!![pluginName]!!, mConfig?.buildBranch)
                                .plus(version).plus(File.separator).plus("proguard/mapping.txt")
                    } else {
                        String.format(mConfig?.mBuiltInPluginRepo!![pluginName]!!, mConfig?.buildBranch)
                                .plus(version).plus(File.separator).plus("mapping.txt")
                    }
                    println("$pluginName:$mapping")
                    println("开始$pluginName-mapping")
                    DownloadUtil.get().download(mapping, "../tmp", pluginName.plus("-").plus(vCode).plus("-"), object : DownloadUtil.OnDownloadListener {
                        override fun onDownloadFailed(e: Exception) {
                            println(e.toString())
                        }

                        override fun onDownloadSuccess() {

                        }

                        override fun onDownloading(progress: Int) {
                            print(progress)
                        }
                    })
                }
            }
        } finally {
            println(Constant.LOGGER_TAG.plus("mapping链接获取：").plus(System.currentTimeMillis() - time).plus("ms"))
        }
    }

    override fun collectMapping(config: IConfig): Map<String, Pair<String, String>> {
        this.mConfig = config
        preConfig()
        downloadMappings()
        val map = HashMap<String, Pair<String, String>>()
        val time = System.currentTimeMillis()
        val fileList = config.getMappings()
        for (path in fileList) {
            if (config.getPackageFilter()?.pluginAllow(path)!!) {
                map.putAll(parseMappings(path, Charsets.UTF_8.name()))
            }
        }
        Log.d(Constant.LOGGER_TAG, "parse map 花费时间:".plus(System.currentTimeMillis() - time))
        return map
    }

    /**
     * map查找key速度优于list
     */
    private fun parseMappings(filePath: String, charsetName: String): Map<String, Pair<String, String>> {
        val file = File(filePath)
        val fileContent = HashMap<String, Pair<String, String>>()
        if (!file.isFile) {
            return fileContent
        }
        var reader: BufferedReader? = null
        try {
            val `is` = InputStreamReader(FileInputStream(file), charsetName)
            reader = BufferedReader(`is`)
            while (true) {
                val line = reader.readLine() ?: break
                val pkgPair = extractPkg(line)
                if (!TextUtils.isEmpty(pkgPair.first)) {
                    if (mConfig?.getPackageFilter()?.pkgAllow(pkgPair.first)!!) {
                        fileContent[pkgPair.first] = pkgPair
                    }
                    Log.d("Mappings", pkgPair.toString())
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

    private fun extractPkg(content: String): Pair<String, String> {
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
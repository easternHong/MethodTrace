package mapping

import utils.Constant
import utils.IConfig
import utils.Log
import utils.TextUtils
import java.io.*


/**
 * 对应插件mappings文件处理，提取mappings.txt文件中的class
 */
class Mappings : IMapping {
    var mConfig: IConfig? = null
    override fun collectMapping(config: IConfig): Map<String, String> {
        this.mConfig = config
        val map = HashMap<String, String>()
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
    private fun parseMappings(filePath: String, charsetName: String): Map<String, String> {
        val file = File(filePath)
        val fileContent = HashMap<String, String>()
        if (!file.isFile) {
            return fileContent
        }
        var reader: BufferedReader? = null
        try {
            val `is` = InputStreamReader(FileInputStream(file), charsetName)
            reader = BufferedReader(`is`)
            while (true) {
                val line = reader.readLine() ?: break
                val pkg = extractPkg(line)
                if (!TextUtils.isEmpty(pkg)) {
                    if (mConfig?.getPackageFilter()?.pkgAllow(pkg)!!) {
                        fileContent[pkg] = pkg
                    }
                    Log.d("Mappings", pkg)
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

    private fun extractPkg(content: String): String {
        if (content.startsWith(" ")) return ""
        val index = content.indexOf("->")
        if (index > 0) {
            return content.substring(0, index).replace(" ", "")
        }
        return ""
    }

    companion object {
        val mappings = Mappings()
    }

}
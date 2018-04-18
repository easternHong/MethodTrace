package map

import utils.Log
import utils.TextUtils
import java.io.*


/**
 * 对应插件mappings文件处理，提取mappings.txt文件中的class
 */
class Mappings {

    /**
     * map查找key速度优于list
     */
    fun parseMappings(filePath: String, charsetName: String): Map<String, String> {
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
                    fileContent[pkg] = pkg
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

    //
//    @JvmStatic
//    fun main(args: Array<String>) {
//        parseMappings("/home/g8489/yy/7.7.0_dior_feature/entmobile-android_7.7.0_dior_feature/client/mapping.txt", Charsets.UTF_8.name())
//    }
    companion object {
        val mappings = Mappings()
    }

}
package diff

import java.io.*
import java.util.*
import kotlin.collections.ArrayList


object PackageDiff {

    @JvmStatic
    fun main(args: Array<String>) {
        val oldMap = getClasses("/home/g8489/diff/6231-main.txt")
        val newMap = getClasses("/home/g8489/diff/6323-main.txt")
        val keySet = HashSet(newMap.keys)
        val newAddClassList = ArrayList<String>()
        println("start")
        for (key in keySet) {
            val clazzOld = oldMap[key]
            val clazzNew = newMap[key]
            if (clazzOld == clazzNew) {
                oldMap.remove(key)
                newMap.remove(key)
            }
            if (clazzOld == null) {
                newAddClassList.add(key)
            }
            if (key.contains("$$$")) {
                println(key)
            }
        }
        val oldValues = TreeSet(oldMap.values)
        val newValues = TreeSet(newMap.values)

        println(oldValues.size)
        println(newValues.size)
        println("newClass:" + newAddClassList.size)
    }

    private fun getClasses(filePath: String): HashMap<String, ClassDesc> {
        val file = File(filePath)
        if (!file.isFile) {
            return HashMap()
        }
        var reader: BufferedReader? = null
        val map = HashMap<String, ClassDesc>()
        try {
            val `is` = InputStreamReader(FileInputStream(file), Charsets.UTF_8.name())
            reader = BufferedReader(`is`)
            var pkgName = ""
            while (true) {
                var line = reader.readLine() ?: break
                if (!line.startsWith(" ")) {
                    pkgName = extractPkg(line)
                    if (pkgName.contains("DartsFactory")) {
                        if (pkgName.endsWith("Instance")) {
                            pkgName = pkgName.substring(pkgName.lastIndexOf("$") + 1)
                        } else continue
                    }
                    if (pkgName.isEmpty()) continue
                    map[pkgName] = ClassDesc()
                } else {
                    val classDesc = map[pkgName]
                    if (line.contains("(")) {
                        //method
                        line = line.substring(0, line.indexOf("->"))
                        if (line.contains(":")) {
                            line = line.substring(line.lastIndexOf(":") + 1)
                        }
                        if (line.contains("<init>")) continue //忽略构造函数
                        if (line.contains("<clinit>")) continue //忽略构造函数
                        classDesc?.pkgName = pkgName
                        classDesc?.methodList?.add(line)
                    } else {
                        //field
                        val field = line.substring(0, line.indexOf("->")).trim()
                        classDesc?.filedList?.add(field)
                        println("field:$field")
                    }
                }
            }
            reader.close()
            return map
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
}

class ClassDesc : Comparable<ClassDesc> {

    override fun compareTo(other: ClassDesc): Int {
        return this.pkgName.compareTo(other.pkgName)
    }

    var filedList = ArrayList<String>()
    var pkgName = ""
    var methodList = ArrayList<String>()

    override fun toString(): String {
        return "ClassDesc(pkgName='$pkgName', methodList=$methodList)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassDesc

        if (filedList != other.filedList) return false
        if (pkgName != other.pkgName) return false
        if (methodList != other.methodList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filedList.hashCode()
        result = 31 * result + pkgName.hashCode()
        result = 31 * result + methodList.hashCode()
        return result
    }


}
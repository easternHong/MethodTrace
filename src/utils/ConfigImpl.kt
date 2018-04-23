package utils

import java.io.File


class ConfigImpl : IConfig() {

    override fun getMappings(): List<String> {
        val list = ArrayList<String>()
        val fList = File(workingDir).listFiles { p0, p1 -> p1.endsWith("mapping.txt") }

        for (f in fList) {
            list.add(f.absolutePath)
        }
        mappingsFilePaths = list
        return list
    }
}
object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val filePath = "/Users/eastern/project/TraceAnalyze/test.trace"
        val reader = DmTraceReader(filePath, true)
        reader.setDumpFilter(object : DumpFilter {
            override fun allow(content: String): Boolean {
//                return content.contains("eastern")
                return true
            }
        })
        reader.generateTrees()
        reader.methods
                .filter { it.methodName == "onCreate" }
                .forEach { println("get you:$it") }




    }
}
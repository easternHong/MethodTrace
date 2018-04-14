object Main {

    @JvmStatic
    fun main(args: Array<String>) {


        val reader = DmTraceReader("/Users/eastern/project/TraceAnalyze/test.trace", true)
        reader.setDumpFilter(object : DumpFilter {
            override fun allow(content: String): Boolean {
                return content.contains("eastern")
            }
        })
        reader.generateTrees()
        println("end?")


    }
}
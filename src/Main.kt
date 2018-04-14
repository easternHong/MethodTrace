object Main {

    @JvmStatic
    fun main(args: Array<String>) {


        val reader = DmTraceReader("/Users/eastern/project/TraceAnalyze/test.trace", true)

        println("end?")
        println(reader.threadTimeRecords)

    }
}
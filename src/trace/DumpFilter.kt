package trace

interface DumpFilter {


    fun allow(methodData: MethodData): Boolean
}

interface FormatListener {
    fun finish()
}
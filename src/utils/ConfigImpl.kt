package utils


class ConfigImpl : IConfig() {

    override fun getMappings(): List<String> {
        return mappingsFilePaths
    }
}
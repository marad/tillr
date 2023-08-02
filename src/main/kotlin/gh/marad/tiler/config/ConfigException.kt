package gh.marad.tiler.config

import java.lang.RuntimeException

open class ConfigException(message: String) : RuntimeException(message) {
    constructor() : this("Configuration exception")
}
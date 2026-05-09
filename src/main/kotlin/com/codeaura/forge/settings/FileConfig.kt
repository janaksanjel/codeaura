package com.codeaura.forge.settings

data class FileConfig(
    var removeComments: Boolean = false,
    var cleanImports: Boolean = false,
    var autoFormat: Boolean = false,
    var fixBrackets: Boolean = false,
    var sortLines: Boolean = false,
    var trimWhitespace: Boolean = false
)

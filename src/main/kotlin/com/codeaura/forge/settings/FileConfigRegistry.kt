package com.codeaura.forge.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VirtualFile

@Service
class FileConfigRegistry {
    private val configs = mutableMapOf<String, FileConfig>()

    fun getOrCreate(file: VirtualFile): FileConfig =
        configs.getOrPut(file.path) { FileConfig() }

    fun get(file: VirtualFile): FileConfig? = configs[file.path]

    fun remove(file: VirtualFile) = configs.remove(file.path)
}

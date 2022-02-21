package utils

import java.io.File

object Runtime {
    const val BLOCKS_DIR = "blocks"
    const val SOUNDS_DIR = "sounds"
    const val TASKS_DIR = "tasks"
    const val LOGS_DIR = "logs"

    fun getSourceCodePath(
        clazz: Class<*>, defaultIfErrorOrWithinIde: String = System.getProperty("java.io.tmpdir")): String {
        val resource = clazz.getResource("")

        val ret by lazy {
            if (resource != null && resource.protocol == "jar") {
                try {
                    File(clazz.protectionDomain.codeSource.location.toURI().path).parentFile.path
                } catch (any: Exception) {
                    defaultIfErrorOrWithinIde
                }
            } else defaultIfErrorOrWithinIde
        }

        return ret
    }

    fun isWithinJar(clazz: Class<*>) : Boolean {
        val resource = clazz.getResource("")

        return (resource != null && resource.protocol == "jar")
    }

    fun replacePathWithRuntimePath(filePath: String, runtimeRoot: String, doFileChecks: Boolean? = true) : String? {
        return replacePathWithRuntimePath(File(filePath), runtimeRoot, doFileChecks)
    }

    fun replacePathWithRuntimePath(file: File, runtimeRoot: String, doFileChecks: Boolean? = true) : String? {
        if (doFileChecks == true && (!file.exists() || !file.isFile || !file.canRead())) return null

        val name = transfer.extractFileNameForPotentialWindowsFilePath(file)
        val root = File(runtimeRoot)
        if (root.exists() && (!root.isDirectory || !root.canRead())) return null
        if (!root.exists()) root.mkdir()

        return File(root, name).absolutePath
    }
}
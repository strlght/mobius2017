package me.strlght

import java.io.File

/**
 * @author Grigoriy Dzhanelidze
 */
class AssertionProcessor(
        private val inputPath: File,
        private val sourcePath: File,
        private val outputPath: File,
        private val classpath: Collection<File>,
        private val bootClasspath: Collection<File>
) {
    fun process() {
        Patcher().copyAndPatchClasses(inputPath, outputPath)
    }
}
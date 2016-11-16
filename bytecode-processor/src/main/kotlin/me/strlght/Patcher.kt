package me.strlght

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import java.io.File

/**
 * @author Grigoriy Dzhanelidze
 */
class Patcher {
    fun copyAndPatchClasses(inputPath: File, outputPath: File) {
        for (sourceFile in inputPath.walk()) {
            val relativePath = sourceFile.toRelativeString(inputPath)
            val targetFile = File(outputPath, relativePath)
            if (sourceFile.isFile) {
                val type = getObjectTypeFromFile(relativePath)
                if (type != null) {
                    patchClass(sourceFile, targetFile)
                }
            } else {
                targetFile.mkdirs()
            }
        }
    }

    private fun getObjectTypeFromFile(relativePath: String): Type? {
        if (relativePath.endsWith(".class")) {
            val internalName = relativePath.substringBeforeLast(".class")
            return Type.getObjectType(internalName)
        }
        return null
    }

    private fun patchClass(sourceFile: File, targetFile: File) {
        val reader = ClassReader(sourceFile.readBytes())
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        val visitor = AssertionVisitor(writer)
        reader.accept(visitor, ClassReader.SKIP_FRAMES)
        targetFile.parentFile.mkdirs()
        targetFile.writeBytes(writer.toByteArray())
    }
}
import com.android.ide.common.vectordrawable.Svg2Vector
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class SvgFilesProcessor(
    private val sourceSvgDirectory: String,
    private val destinationVectorDirectory: String = "",
    private val replaceContent: Map<String, String> = emptyMap()
) {
    private val sourceSvgPath: Path by lazy {
        Paths.get(sourceSvgDirectory)
    }
    private val destinationVectorPath: Path by lazy {
        Paths.get(destinationVectorDirectory.ifEmpty {
            "$sourceSvgDirectory/$OUTPUT_DIR"
        })
    }

    fun process() {
        try {
            val options: Set<FileVisitOption> = EnumSet.of(FileVisitOption.FOLLOW_LINKS)
            var totalFilesCount = 0
            var convertedFilesCount = 0
            val ignoredFiles = mutableListOf<String>()
            //check first if source is a directory
            if (Files.isDirectory(sourceSvgPath)) {
                val visitor = object : SimpleFileVisitor<Path>() {

                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        totalFilesCount++
                        if (file.toString().endsWith(".svg")) {
                            try {
                                convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)))
                                convertedFilesCount++
                            } catch (e: Exception) {
                                ignoredFiles.add(file.fileName.toString())
                                println("Exception: ${e.message}")
                            }
                        } else {
                            ignoredFiles.add(file.fileName.toString())
                        }
                        return FileVisitResult.CONTINUE
                    }
                }
                Files.walkFileTree(sourceSvgPath, options, Int.MAX_VALUE, visitor)
                println("----")
                println("Total files: $totalFilesCount")
                println("Converted files: $convertedFilesCount")
                println("Ignored files: ${ignoredFiles.size}")
                println("----")
                println("Operation completed:")
            } else {
                println("Source is not a directory")
            }
        } catch (e: IOException) {
            println("IOException " + e.message)
        }
    }

    @Throws(IOException::class)
    private fun convertToVector(source: Path, target: Path): Boolean {
        // convert only if it is .svg
        if (source.fileName.toString().endsWith(".svg")) {
            try {
                val vectorContent = convertSvgToVector(source.toFile())
                val targetFile = getFileWithXMlExtension(target)
                val fileOutputStream = FileOutputStream(targetFile)
                fileOutputStream.write(vectorContent.toByteArray())
                println("Converted: ${source.fileName}")
                return true
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        } else {
            println("Skipping file as its not svg " + source.fileName.toString())
        }
        return false
    }

    private fun convertSvgToVector(source: File): String {
        val outputStream = ByteArrayOutputStream()
        Svg2Vector.parseSvgToXml(source, outputStream)
        val result = outputStream.toString()
        return replaceContents(result)
    }

    private fun replaceContents(svgContent: String): String {
        // Replace color codes in SVG content
        var modifiedSvgContent = svgContent
        for ((oldContent, newContent) in replaceContent) {
            modifiedSvgContent = modifiedSvgContent.replace(oldContent, newContent)
        }
        return modifiedSvgContent
    }

    private fun getFileWithXMlExtension(target: Path): File {
        val svgFilePath = target.toFile().absolutePath
        val svgBaseFile = StringBuilder()
        val index = svgFilePath.lastIndexOf(".")
        if (index != -1) {
            val subStr = svgFilePath.substring(0, index)
            svgBaseFile.append(subStr)
        }
        svgBaseFile.append(".")
        svgBaseFile.append(OUTPUT_EXTENSION)
        return File(svgBaseFile.toString())
    }

    companion object {
        const val OUTPUT_DIR = "output"
        const val OUTPUT_EXTENSION = "xml"
    }
}
import com.android.ide.common.vectordrawable.Svg2Vector
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.pathString

class SvgFilesProcessor(
    private val sourceSvgDirectory: String,
    private val destinationVectorDirectory: String = "",
    private val extension: String = "xml",
    private val extensionSuffix: String = "",
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
            println("Destination path: ${destinationVectorPath.pathString}")
            val options: Set<FileVisitOption> = EnumSet.of(FileVisitOption.FOLLOW_LINKS)
            //check first if source is a directory
            if (Files.isDirectory(sourceSvgPath)) {
                Files.walkFileTree(sourceSvgPath, options, Int.MAX_VALUE, object : FileVisitor<Path> {
                    @Throws(IOException::class)
                    override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                        if (exc != null) {
                            println("IOException occurred: ${exc.message}")
                        }
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(
                        dir: Path,
                        attrs: BasicFileAttributes
                    ): FileVisitResult {
                        // Skip folder which is processing svgs to xml
                        if (dir == destinationVectorPath) {
                            return FileVisitResult.SKIP_SUBTREE
                        }
                        val opt =
                            arrayOf<CopyOption>(StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
                        val newDirectory = destinationVectorPath.resolve(sourceSvgPath.relativize(dir))
                        try {
                            Files.copy(dir, newDirectory, *opt)
                        } catch (ex: FileAlreadyExistsException) {
                            println("FileAlreadyExistsException $ex")
                        } catch (x: IOException) {
                            return FileVisitResult.SKIP_SUBTREE
                        }
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes
                    ): FileVisitResult {
                        convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)))
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun visitFileFailed(
                        file: Path,
                        exc: IOException
                    ): FileVisitResult {
                        return FileVisitResult.CONTINUE
                    }
                })
            } else {
                println("source not a directory")
            }
        } catch (e: IOException) {
            println("IOException " + e.message)
        }
    }

    @Throws(IOException::class)
    private fun convertToVector(source: Path, target: Path) {
        // convert only if it is .svg
        if (source.fileName.toString().endsWith(".svg")) {
            val targetFile = getFileWithXMlExtension(target, extension, extensionSuffix)
            val fous = FileOutputStream(targetFile)
            Svg2Vector.parseSvgToXml(source.toFile(), fous)
            println("Converted: ${source.fileName}")
        } else {
            println("Skipping file as its not svg " + source.fileName.toString())
        }
    }

    private fun getFileWithXMlExtension(target: Path, extension: String, extensionSuffix: String?): File {
        val svgFilePath = target.toFile().absolutePath
        val svgBaseFile = StringBuilder()
        val index = svgFilePath.lastIndexOf(".")
        if (index != -1) {
            val subStr = svgFilePath.substring(0, index)
            svgBaseFile.append(subStr)
        }
        svgBaseFile.append(extensionSuffix ?: "")
        svgBaseFile.append(".")
        svgBaseFile.append(extension)
        return File(svgBaseFile.toString())
    }

    companion object {
        const val OUTPUT_DIR = "output"
    }
}
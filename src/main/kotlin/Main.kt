fun main() {
    println("----")
    println("SVG to Vector")
    println("----")
    val defaultSource: String = System.getProperty("user.dir")
    val defaultDestination = "$defaultSource/output"

    print("Source directory (Default: $defaultSource): ")
    val sourceDir = readlnOrNull() ?: defaultSource

    print("Destination directory (Default: $defaultDestination): ")
    val destinationDir = readlnOrNull() ?: defaultDestination

    print("Do you want to replace any text in output files? [Y/n] ")
    val needReplace = readlnOrNull().let {
        (it ?: "n").lowercase() == "y"
    }
    val replacement = getReplacements(needReplace)

    val processor = SvgFilesProcessor(
        sourceDir,
        destinationDir,
        replacement
    )
    processor.process()
}

fun getReplacements(needReplace: Boolean): Map<String, String> {
    if (!needReplace) return emptyMap()

    val replacements = mutableMapOf<String, String>()

    while (true) {
        print("Enter the text to replace (or press Enter to finish): ")
        val textToReplace = readlnOrNull()

        if (textToReplace.isNullOrBlank()) {
            break
        }

        print("Enter the replacement text: ")
        val replacementText = readlnOrNull() ?: ""

        replacements[textToReplace] = replacementText
    }

    return replacements
}

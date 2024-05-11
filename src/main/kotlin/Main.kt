fun main(args: Array<String>) {
    println("Welcome to SvgToVector convertor CLI...")
    println("----")
    if (args.isEmpty()) {
        println("Error: Source directory not specified!")
    } else {
        val sourceDirectory = args[0]
        val processor = SvgFilesProcessor(sourceDirectory)
        processor.process()
    }
}
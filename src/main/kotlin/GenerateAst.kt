import java.io.IOException
import java.io.PrintWriter
import kotlin.system.exitProcess


object GenerateAst {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory>")
            exitProcess(64)
        }
        val outputDir = args[0]
        defineAst(
            outputDir, "Expr", listOf(
                "Binary -> left: Expr, operator: Token, right: Expr",
                "Grouping -> expression: Expr",
                "Literal -> value: Any?",
                "Unary -> operator: Token, right: Expr"
            )
        )
    }

    private fun defineAst(
        outputDir: String,
        baseName: String,
        types: List<String>
    ) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println()
        //writer.println("import java.util.List;")
        writer.println()
        writer.println("abstract class $baseName {")

        for (type in types) {
            val className = type.split("->").firstOrNull()?.trim()
            val fields = type.split("->").getOrNull(1)?.trim()
            defineType(writer, baseName, className, fields)
        }

        writer.println("}")
        writer.close()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String?, fieldList: String?) {
        val fields = fieldList?.split(",")?.map { it.trim() } ?: listOf()
        val fieldsString = fields.joinToString { "val $it" }
        writer.println("    internal class $className($fieldsString) : $baseName()")

    }
}
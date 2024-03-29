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
        writer.println()
        writer.println("abstract class $baseName {")
        defineVisitor(writer, baseName, types)

        for (type in types) {
            val className = type.split("->").firstOrNull()?.trim()
            val fields = type.split("->").getOrNull(1)?.trim()
            defineType(writer, baseName, className, fields)
        }

        writer.println()
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>) : R")

        writer.println("}")
        writer.close()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String?, fieldList: String?) {
        val fields = fieldList?.split(",")?.map { it.trim() } ?: listOf()
        val fieldsString = fields.joinToString { "val $it" }
        writer.println("    class $className($fieldsString) : $baseName() {")
        writer.println("        override fun <R> accept(visitor: Visitor<R>) : R {")
        writer.println("            return visitor.visit$className$baseName(this)")
        writer.println("        }")
        writer.println("    }")
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<R> {")
        types.forEach {
            val typeName = it.split("->").firstOrNull()?.trim()
            writer.println("        fun visit$typeName$baseName(${baseName.lowercase()}: $typeName) : R")
            //writer.println("    R visit $typeName $baseName ($typeName ${baseName.lowercase()})")
        }
        writer.println("    }")
    }
}
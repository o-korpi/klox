import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.exp
import kotlin.system.exitProcess


object Main {
    private var hadError = false

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: klox [script]")
            exitProcess(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }


    private fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path)) ?: throw IOException("fuck!")
        run(String(bytes, Charset.defaultCharset()))
        if (hadError) exitProcess(65)
    }

    private fun runPrompt() {
        while (true) {
            print(">>> ")
            val line = BufferedReader(InputStreamReader(System.`in`)).readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val expression = Parser(Scanner(source).scanTokens()).parse()
        if (hadError) return
        println(AstPrinter().print(expression))
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF)
            report(token.line, " at end", message)
        else
            report(token.line, " at '${token.lexeme}'", message)
    }

    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }

}

import TokenType.*
import kotlin.math.exp

class Parser(private val tokens: List<Token>) {
    internal class ParseError : RuntimeException()
    private var current = 0

    fun parse(): Expr ?= try {
        expression()
    } catch (e: ParseError) {
        null
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun match(vararg types: TokenType): Boolean {
        types.filter { type -> check(type) }.forEach { _ ->
            advance()
            return true
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false
        else peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current += 1
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun binaryCore(vararg types: TokenType, rightMethod: () -> Expr): Expr {
        var expr = rightMethod()
        while (match(*types)) {
            expr = Expr.Binary(expr, previous(), rightMethod())
        }
        return expr
    }


    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val right = comparison()
            expr = Expr.Binary(expr, previous(), right)
        }

        return expr
    }

    private fun comparison(): Expr = binaryCore(GREATER_EQUAL, GREATER, LESS, LESS_EQUAL) { term() }

    private fun term(): Expr = binaryCore(PLUS, MINUS) { factor() }

    private fun factor(): Expr = binaryCore(STAR, SLASH) { unary() }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            return Expr.Unary(previous(), unary())
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Main.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            if (peek().type in setOf(
                CLASS,
                FUN,
                VAR,
                FOR,
                IF,
                WHILE,
                PRINT,
                RETURN
            ))

            advance()
        }
    }

}
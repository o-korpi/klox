import TokenType.*

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

    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val right = comparison()
            expr = Expr.Binary(expr, previous(), right)
        }

        return expr
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


    private fun core(expr: Expr, rightMethod: () -> Expr): Expr.Binary {
        return Expr.Binary(expr, previous(), rightMethod())
    }
    private fun comparison(): Expr {
        var expr = term()

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            expr = core(expr) { term() }
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

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

        if (match(LEFT_BRACE)) {
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expression())
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
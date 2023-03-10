import TokenType.*
import java.util.EmptyStackException
import java.util.Stack

class Scanner(
    private val source: String, private val tokens: MutableList<Token> = mutableListOf()
) {
    private var start = 0;
    private var current = 0;
    private var line = 1;
    private val keywords = mapOf(
        "and" to AND,
        "or" to OR,
        "class" to CLASS,
        "super" to SUPER,
        "this" to THIS,
        "if" to IF,
        "else" to ELSE,
        "fun" to FUN,
        "return" to RETURN,
        "for" to FOR,
        "while" to WHILE,
        "var" to VAR,
        "print" to PRINT,
        "nil" to NIL,
        "true" to TRUE,
        "false" to FALSE
    )

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                // Catch comments
                if (match('/')) {
                    // Continue advancing until EOF or EOL
                    while(peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    longComment()
                }else
                    addToken(SLASH)
            }
            ' ', '\r', '\t' -> {/* ignored */}
            '\n' -> line += 1
            '"' -> string()
            else -> {
                if (isDigit(c)) number()
                else if (isAlpha(c)) identifier()
                else Main.error(line, "Unexpected character $c.")
            }
        }
    }

    private fun peek(): Char? {
        if (isAtEnd()) return null
        return source[current]
    }

    private fun peekNext(): Char? {
        if (current + 1 >= source.length) return null
        return source[current + 1]
    }

    // Similar to advance, but only consumes character if it matches
    private fun match(char: Char): Boolean {
        if (isAtEnd() || source[current] != char) return false
        current += 1
        return true
    }

    private fun advance(): Char {
        val toRet = source[current]
        current += 1
        return toRet
    }

    private fun addToken(type: TokenType): Unit = addToken(type, null)

    private fun addToken(type: TokenType, literal: Any?) {
        tokens.add(Token(type, source.substring(start, current), literal, line))
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line += 1
            advance()
        }

        if (isAtEnd()) {
            Main.error(line, "Unterminated string.")
            return
        }

        advance()

        addToken(
            STRING,
            source.substring(start + 1, current - 1)
        )
    }

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()
        }

        while (isDigit(peek())) advance()

        addToken(NUMBER, source.substring(start, current))
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        addToken(keywords[source.substring(start, current)] ?: IDENTIFIER)
    }

    private fun longComment() {
        val startingLine = line
        while (!isAtEnd()) {
            if (peek() == '\n') line += 1
            if (peek() == '/' && peekNext() == '*') {
                advance()
                advance()
                longComment()
            }
            else if (peek() == '*' && peekNext() == '/') {
                advance()
                advance()
                return
            }
            advance()
        }
        Main.error(startingLine, "Unclosed comment.")
    }

    private fun isDigit(c: Char?) = c in '0'..'9'
    private fun isAlpha(c: Char?) = (c in 'a'..'z') || (c in 'A'..'Z') || (c == '_')
    private fun isAlphaNumeric(c: Char?) = isDigit(c) || isAlpha(c)
}
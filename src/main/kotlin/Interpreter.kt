import kotlin.reflect.typeOf

class Interpreter : Expr.Visitor<Any?> {


    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.MINUS -> {

            }
            else -> null
        }
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

}
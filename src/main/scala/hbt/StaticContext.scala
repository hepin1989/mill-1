package hbt


import scala.language.experimental.macros
import scala.reflect.macros._

case class StaticContext(value: Boolean)
object StaticContext {
  implicit def default: StaticContext = macro applyImpl
  def rec(c: Context)(expr: c.Symbol): Boolean = {
    import c.universe._
    // Classes and traits and such
    if(!expr.isModuleClass && expr.isClass) false
    // Method contents
    else if(expr.isMethod) false
    else if(expr.owner == NoSymbol) true
    else rec(c)(expr.owner)
  }

  def applyImpl(c: Context): c.Expr[StaticContext] = {
    import c.universe._
    val staticContext = rec(c)(c.internal.enclosingOwner)
    c.Expr[StaticContext](q"hbt.StaticContext($staticContext)")
  }
}
case class DefCtx(staticEnclosing: Option[String])
object DefCtx{
  implicit def default(implicit enc: sourcecode.Enclosing,
                       sc: StaticContext) = {
    if (sc.value) DefCtx(Some(enc.value))
    else DefCtx(None)
  }
}
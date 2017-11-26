import java.nio.file.{Path, Paths}
import org.apache.ivy.Ivy
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.plugins.matcher.{PatternMatcher, RegexpPatternMatcher}

object Strike1 {

  val ivy: Ivy = Ivy.newInstance()
//  ivy.configureDefault() // not sure what this does
  val ivyHome: Path = Paths.get(sys.props.getOrElse("ivy.home", sys.props("user.home") + "/.ivy2"))

  val modules: Array[ModuleRevisionId] = {
    val criteria = ModuleRevisionId.newInstance(
      PatternMatcher.ANY_EXPRESSION,
      PatternMatcher.ANY_EXPRESSION,
      PatternMatcher.ANY_EXPRESSION,
      PatternMatcher.ANY_EXPRESSION
    )
    ivy.getSearchEngine.listModules(criteria, RegexpPatternMatcher.INSTANCE)
  }

  println(modules)
}

import java.net.URL
import java.nio.file.{Path, Paths}
import org.apache.commons.io.FileUtils
import org.apache.ivy.Ivy
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.ResolveOptions
import scala.collection.JavaConverters._

class Strike2(implicit ivy: Ivy) {
  findDependency("jackson-core-2.5.4.jar")

  val ivyHome: Path = Paths.get(sys.props.getOrElse("ivy.home", sys.props("user.home") + "/.ivy2"))

  val confs = Array("default")
  val resolveOptions: ResolveOptions = new ResolveOptions()
    .setDownload(false)
    .setTransitive(true)
    .setConfs(confs)

  def findDependency(jarFileName: String): Unit = {
    FileUtils.listFiles(ivyHome.toFile, Array("jar"), true).asScala.find { file =>
      file.getName == jarFileName
    }.foreach { file =>
      val url: URL = file.toURI.toURL

      // [Fatal Error] jackson-core-2.5.4.jar:1:1: Content is not allowed in prolog.
      val resolveReport2: ResolveReport = ivy.getResolveEngine.resolve(url)
      println(resolveReport2)

      // [Fatal Error] jackson-core-2.5.4.jar:1:1: Content is not allowed in prolog.
      val resolveReport1: ResolveReport = ivy.resolve(url, resolveOptions)
      println(resolveReport1)
    }
  }
}

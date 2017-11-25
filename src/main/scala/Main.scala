import java.net.URL
import java.nio.file.{Path, Paths}
import org.apache.commons.io.FileUtils
import org.apache.ivy.Ivy
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.ResolveOptions
import scala.collection.JavaConverters._

class ClassFileHelper {
  def getClassUrl(clazz: Class[_] ): URL = clazz.getResource('/' + clazz.getName.replace('.', '/') + ".class")

  def getJarFromUrl(url: URL): String = {
    assert(url.getProtocol == "jar")
    val fileName = url.getFile
    val bang = fileName.substring(0, fileName.lastIndexOf('!'))
    bang.substring(fileName.lastIndexOf('/') + 1)
  }
}

object Main extends App {
  val confs = Array("default")
  val resolveOptions = new ResolveOptions()
    .setDownload(false)
    .setTransitive(true)
    .setConfs(confs)

  val ivy: Ivy = Ivy.newInstance()
  val ivyHome: Path = Paths.get(sys.props.getOrElse("ivy.home", sys.props("user.home") + "/.ivy2"))

  FileUtils.listFiles(ivyHome.toFile, Array("jar"), true).asScala.find { file =>
    file.getName == "jackson-core-2.5.4.jar"
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

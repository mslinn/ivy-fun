import java.io.{File, InputStream}
import java.net.{JarURLConnection, URL}
import java.nio.file.Path
import java.util.jar
import org.apache.commons.io.FileUtils

object JarManifest {
 /* def getManifest(url: URL): jar.Manifest = {
    url.openConnection.getManifest
  }

  def getManifest(file: File): jar.Manifest = getManifest(file.toURI.toURL)

  def getManifest(path: Path): jar.Manifest = getManifest(path.toFile.toURI.toURL)
*/
  def getManifest(string: String): jar.Manifest = {
    val file = new File(string)
    assert(file.exists)
    val is: InputStream = FileUtils.openInputStream(file)
    val result = new jar.Manifest(is)
    result
  }

  val manifest: jar.Manifest = {
    val x: String = s"${ sys.props("user.home") }/.sbt/boot/scala-2.12.4/lib/scala-library.jar".replace("/", File.separator)
    val result = getManifest(x)
    result
  }
}

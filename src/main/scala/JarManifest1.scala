import java.io.{File, InputStream}
import java.util.jar
import org.apache.commons.io.FileUtils

object JarManifest1 {
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

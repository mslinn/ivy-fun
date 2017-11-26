import java.io.File
import java.net.{JarURLConnection, URL}
import java.nio.file.{Path, Paths}
import java.util.jar

object JarManifest {
  def getManifest(url: URL): jar.Manifest = url.openConnection.asInstanceOf[JarURLConnection].getManifest

  def getManifest(file: File): jar.Manifest = getManifest(file.toURI.toURL)

  def getManifest(path: Path): jar.Manifest = getManifest(path.toFile.toURI.toURL)

  def getManifest(string: String): jar.Manifest = getManifest(new URL(string))

  getManifest(new URL("jar:file:/home/duke/duke.jar!/"))
}

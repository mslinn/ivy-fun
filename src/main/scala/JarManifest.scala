import java.net.{JarURLConnection, URL}
import java.util
import java.util.jar.{Attributes, JarFile, Manifest}
import scala.collection.JavaConverters._

class JarManifest(path: String) {
  val jarUrlString = s"jar:file:/$path!/"
  val filePathString = s"file:/$path"

  val fileSysUrl = new URL(jarUrlString)
  val jarURLConnection: JarURLConnection = fileSysUrl.openConnection.asInstanceOf[JarURLConnection]
  val jarFile: JarFile = jarURLConnection.getJarFile
  val manifest: Manifest = jarFile.getManifest

  val manifestAttributes: Map[String, AnyRef] =
    manifest
      .getMainAttributes
      .entrySet
      .asScala
      .toSet
      .toList
      .map { x: util.Map.Entry[Object, Object] => x.getKey.asInstanceOf[Attributes.Name].toString -> x.getValue }
      .toMap
}


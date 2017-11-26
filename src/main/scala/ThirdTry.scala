import java.io.File
import java.lang.reflect.Method
import java.net.{JarURLConnection, URL, URLClassLoader}
import java.util
import java.util.jar.{Attributes, JarFile, Manifest}
import scala.collection.JavaConverters._

class JarManifest2(path: String) {
  val jarUrlString = s"jar:file:/$path!/"
  val filePathString = s"file:/$path"

  val fileSysUrl = new URL(jarUrlString)
  val jarURLConnection: JarURLConnection = fileSysUrl.openConnection.asInstanceOf[JarURLConnection]
  val jarFile: JarFile = jarURLConnection.getJarFile
  val manifest: Manifest = jarFile.getManifest

  val manifestAttributes: Map[AnyRef, AnyRef] =
    manifest
      .getMainAttributes
      .entrySet
      .asScala
      .toSet
      .toList
      .map { x: util.Map.Entry[Object, Object] => x.getKey -> x.getValue }
      .toMap
}

object ThirdTry {
  val path: String = s"${ sys.props("user.home") }/.sbt/boot/scala-2.12.4/lib/scala-library.jar".replace("/", File.separator)
  val JAR_URL = s"jar:file:/$path!/"
  val JAR_FILE_PATH = s"file:/$path"
}

/** @see See [[https://examples.javacodegeeks.com/core-java/net/jarurlconnection/java-net-jarurlconnection-example/ java.net.JarURLConnection Example]]*/
class ThirdTry {
  import ThirdTry._

  // Create a URL that refers to a jar file in the file system
  val fileSysUrl = new URL(JAR_URL)

  // Create a jar URL connection object
  val jarURLConnection: JarURLConnection = fileSysUrl.openConnection.asInstanceOf[JarURLConnection]

  // Get the jar file
  val jarFile: JarFile = jarURLConnection.getJarFile

  // Get jar file name
  println("Jar Name: " + jarFile.getName)

  // When no entry is specified on the URL, the entry name is null
  println("\nJar Entry: " + jarURLConnection.getJarEntry)

  // Get the manifest of the jar
  val manifest: Manifest = jarFile.getManifest

  // Print the manifest attributes
  println("\nManifest file attributes: ")
  manifest.getMainAttributes.entrySet.asScala.foreach { entry =>
    println(entry.getKey + ": " + entry.getValue)
  }

  println("\nExternal JAR Execution output: ")
  // Get the jar URL, which contains target class
  val classLoaderUrls: Array[URL] = Array[URL](new URL(JAR_FILE_PATH))

  // Create a classloader and load the entry point class
  val urlClassLoader = new URLClassLoader(classLoaderUrls)

  // Get the main class name (the entry point class), if it is defined
  Option(manifest.getMainAttributes.getValue(Attributes.Name.MAIN_CLASS)).foreach { mainClassName: String =>
    // Load the target class
    val beanClass: Class[_] = urlClassLoader.loadClass(mainClassName)

    // Get the main method from the loaded class and invoke it
    val method: Method = beanClass.getMethod("main", classOf[Array[String]])

    // init params accordingly
    val params = Array.empty[String]

    // static method doesn't have an instance
    method.invoke(null, params)
  }
}

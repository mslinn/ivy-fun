import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager
import org.apache.ivy.core.module.descriptor.ModuleDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.plugins.repository.Resource

/** Given a list of paths to jars of the form: /home/mslinn/.sbt/boot/scala-2.12.4/lib/scala-library.jar,
  * discover their javadoc/scaladoc urls from information in the Jar manifest.
  * Return a stringified tuple (using # as a delimiter) that contains the path to the jar and the libraries' documentation URL.
  * For example:
  *   /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar#http://docs.oracle.com/javase/8/docs/api/
  *
  * A list of these tuples will be used as a value for Scaladoc's -doc-external-doc command line option
  *
  * @see See [[https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#Main_Attributes Main Attributes]] */
class Test {
  import Global._

  val pathStr: String = s"$ivyHome/cache/com.fasterxml.jackson.core/jackson-core/jars/jackson-core-2.5.4.jar".replace("/", File.separator)

  val attributes: Map[String, AnyRef] = new JarManifest(pathStr).manifestAttributes

  val docUrl: String  = attribute("Bundle-DocURL").mkString

  /** The value is a string id that uniquely defines the organization that maintains the  extension implementation.
    * @deprecated("This attribute may be ignored in a future release", "java 8?") */
  val groupId: String  = attribute("Implementation-Vendor-Id").mkString

  val artifactId: String  = attribute("Bundle-SymbolicName").mkString.substring(groupId.length+1)

  /** This attribute specifies a URL that can be used to obtain the most recent version of the extension if the required version is not already installed.
    * @deprecated("This attribute may be ignored in a future release", "java 8?") */
  val url: URL  = new URL(attribute("Implementation-URL").getOrElse("http://empty"))

  // is Specification-Version or Implementation-Version better?
  val version: String  = attribute("Bundle-Version").mkString

  def withExtension(file: File, extension: String): File = {
    val fileName = file.getName
    val fn2 = if (fileName.contains("."))
        fileName.substring(0, fileName.lastIndexOf('.'))
    else fileName
    new File(file.getParentFile, s"$fn2.$extension")
  }

  val jacksonCoreIvyXml = "C:\\Users\\mslin_000\\.ivy2\\cache\\com.fasterxml.jackson.core\\jackson-core\\ivy-2.7.4.xml"
  val resolveReport: ResolveReport = ivy.resolve(new File(jacksonCoreIvyXml))
  val moduleDescriptor: ModuleDescriptor = resolveReport.getModuleDescriptor
  val resource: Resource = moduleDescriptor.getResource
  val revId: ModuleRevisionId = moduleDescriptor.getModuleRevisionId
  val moduleRevisionId: ModuleRevisionId = moduleDescriptor.getResolvedModuleRevisionId

  println(s"groupId = $groupId")
  println(s"artifactId = $artifactId")
  println(s"version = $version")
  println(s"docUrl = $docUrl")

  val file: File = resolveArtifact(groupId, artifactId, version)

//  val resolvedModuleRevision: ResolvedModuleRevision = ivy.findModule(moduleDescriptor.getModuleRevisionId) // no resolver found for junit#junit: check your configuration

  val defaultRepositoryCacheManager = new DefaultRepositoryCacheManager()
//  defaultRepositoryCacheManager.getArchiveFileInCache(revId.getModuleId)


  val ivyXmlFiles: List[File] =
    FileUtils
      .listFiles(ivyHome.resolve("cache").toFile, Array("xml"), true)
      .toArray
      .toList
      .map(_.asInstanceOf[File])

  val files: List[File] = for {
    xmlFile <- ivyXmlFiles
  } yield {
//    val attributes: Map[String, AnyRef] = new JarManifest(pathStr).manifestAttributes

//    def attribute[T](name: String): Option[T] = attributes.collectFirst {
//      case (key, value) if key == name => value.asInstanceOf[T]
//    }

    val resolveReport = ivy.resolve(xmlFile)
    val moduleDescriptor: ModuleDescriptor = resolveReport.getModuleDescriptor
    val revId: ModuleRevisionId = moduleDescriptor.getModuleRevisionId
    val file: File = resolveArtifact(groupId, artifactId, version)
    file
  }


  def attribute[T](name: String): Option[T] = attributes.collectFirst {
    case (key, value) if key == name => value.asInstanceOf[T]
  }

  def dumpAttributes(): Unit =
    attributes.foreach {
      /** This attribute can be used to identify the vendor of an extension implementation if the applet requires an
        * implementation from a specific vendor. The Java Plug-in will compare the value of this attribute with the
        * Implementation-Vendor-Id attribute of the installed extension. */
      case (key, value) if key == "Implementation-Vendor-Id" =>
        println(s"  $key -> ${ value.asInstanceOf[String] }")

      case (key, value) if key == "Export-Package" =>
        val values: Array[String] = value.asInstanceOf[String].split("")
        println(s"  $key -> ${ values.mkString("\n  ") }")

      case (key, value) =>
        println(s"$key -> $value")
    }
}


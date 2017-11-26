import java.io.File
import java.net.URL
import java.nio.file.{Path, Paths}
import org.apache.ivy.Ivy
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager
import org.apache.ivy.core.module.descriptor.{DefaultArtifact, DefaultDependencyDescriptor, DefaultModuleDescriptor, ModuleDescriptor}
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.{IvyNode, ResolveEngine, ResolveOptions, ResolvedModuleRevision}
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
import org.apache.ivy.plugins.repository.Resource
import org.apache.ivy.plugins.resolver.URLResolver

object Main extends App {
  val test = new Test
}

object Test {
  implicit val ivy: Ivy = Ivy.newInstance()//.setResolveEngine(ResolveEngine)

  val ivyHome: Path = Paths.get(sys.props.getOrElse("ivy.home", sys.props("user.home") + "/.ivy2"))
}

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
  import Test._

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

  val file: File = resolveArtifact(groupId, artifactId, version)

//  val resolvedModuleRevision: ResolvedModuleRevision = ivy.findModule(moduleDescriptor.getModuleRevisionId) // no resolver found for junit#junit: check your configuration

  val defaultRepositoryCacheManager = new DefaultRepositoryCacheManager()
//  defaultRepositoryCacheManager.getArchiveFileInCache(revId.getModuleId)


  println(s"groupId = $groupId")
  println(s"artifactId = $artifactId")
  println(s"version = $version")
  println(s"docUrl = $docUrl")


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

  def resolveArtifact(groupId: String, artifactId: String, version: String): File = {
    //creates clear ivy settings
    val ivySettings: IvySettings = new IvySettings()

    //url resolver for configuration of maven repo
    val resolver: URLResolver = new URLResolver()
    resolver.setM2compatible(true)
    resolver.setName("central")

    //you can specify the url resolution pattern strategy
    resolver.addArtifactPattern("http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]")

    //adding maven repo resolver
    ivySettings.addResolver(resolver)

    //set to the default resolver
    ivySettings.setDefaultResolver(resolver.getName())

    //creates an Ivy instance with settings
    val ivy: Ivy = Ivy.newInstance(ivySettings)

    val ivyFile: File = File.createTempFile("ivy", ".xml")
    ivyFile.deleteOnExit()

    val dep = Array(groupId, artifactId, version)

    val md: DefaultModuleDescriptor =
      DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(dep(0), dep(1) + "-caller", "working"))

    val dd: DefaultDependencyDescriptor =
      new DefaultDependencyDescriptor(md, ModuleRevisionId.newInstance(dep(0), dep(1), dep(2)), false, false, true)
    md.addDependency(dd)

    //creates an ivy configuration file
    XmlModuleDescriptorWriter.write(md, ivyFile)

    val confs: Array[String] = Array("default")
    val resolveOptions: ResolveOptions = new ResolveOptions().setConfs(confs)

    //init resolve report
    val report: ResolveReport = ivy.resolve(ivyFile.toURL, resolveOptions)

    //so you can get the jar library
    val jarArtifactFile: File = report.getAllArtifactsReports()(0).getLocalFile

    jarArtifactFile
  }
}

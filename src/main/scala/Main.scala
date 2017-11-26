import java.io.File
import java.net.URL
import java.nio.file.{Path, Paths}
import org.apache.commons.io.FileUtils
import org.apache.ivy.Ivy
import org.apache.ivy.core.module.descriptor.{Artifact, DefaultDependencyDescriptor, DefaultModuleDescriptor, ModuleDescriptor}
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
import org.apache.ivy.plugins.resolver.URLResolver

object Main extends App {
  val test = new Test2
}

object Global {
  implicit val ivy: Ivy = Ivy.newInstance()//.setResolveEngine(ResolveEngine)

  val ivyHome: Path = Paths.get(sys.props.getOrElse("ivy.home", sys.props("user.home") + "/.ivy2"))

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
    ivySettings.setDefaultResolver(resolver.getName)

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
    val report: ResolveReport = ivy.resolve(ivyFile.toURI.toURL, resolveOptions)

    //so you can get the jar library
    val jarArtifactFile: File = report.getAllArtifactsReports()(0).getLocalFile

    jarArtifactFile
  }
}

class Test2 {
  import Global._

  val ivyXmlFiles: List[File] =
    FileUtils
      .listFiles(ivyHome.resolve("cache").toFile, Array("xml"), true)
      .toArray
      .toList
      .map(_.asInstanceOf[File])

  val files: List[File] = for {
    xmlFile <- ivyXmlFiles
  } yield {
    val resolveReport = ivy.resolve(xmlFile)
    val moduleDescriptor: ModuleDescriptor = resolveReport.getModuleDescriptor
    val artifact: Artifact = moduleDescriptor.getAllArtifacts.head
    val name: String = artifact.getModuleRevisionId.getName
    val url: URL = artifact.getUrl

    val revId: ModuleRevisionId = moduleDescriptor.getModuleRevisionId
    val groupId = revId.getOrganisation
    val artifactId = revId.getModuleId.getName
    val version = revId.getRevision
    val file: File = resolveArtifact(groupId, artifactId, version)
    file
  }
}

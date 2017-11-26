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
    val ivySettings: IvySettings = new IvySettings()

    // Url resolver for configuration of maven repo
    val resolver: URLResolver = new URLResolver()
    resolver.setM2compatible(true)
    resolver.setName("central")
    resolver.addArtifactPattern("http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]")

    ivySettings.addResolver(resolver)
    ivySettings.setDefaultResolver(resolver.getName)
    val ivy: Ivy = Ivy.newInstance(ivySettings)

    val ivyFile: File = File.createTempFile("ivy", ".xml")
    ivyFile.deleteOnExit()

    val md: DefaultModuleDescriptor =
      DefaultModuleDescriptor
        .newDefaultInstance(ModuleRevisionId.newInstance(groupId, s"$artifactId-caller", "working"))

    val revId = ModuleRevisionId.newInstance(groupId, artifactId, version)
    val dd: DefaultDependencyDescriptor = new DefaultDependencyDescriptor(md, revId, false, false, true)
    md.addDependency(dd)

    // Create an ivy configuration file
    XmlModuleDescriptorWriter.write(md, ivyFile)

    val resolveOptions: ResolveOptions = new ResolveOptions().setConfs(Array("default"))

    val report: ResolveReport = ivy.resolve(ivyFile.toURI.toURL, resolveOptions)
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
    resolveReport <- try { List(ivy.resolve(xmlFile)) } catch { case e: Exception => Nil }
    moduleDescriptor <- try { List(resolveReport.getModuleDescriptor) } catch { case e: Exception => Nil }
    artifact <- moduleDescriptor.getAllArtifacts.toList
  } yield {
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

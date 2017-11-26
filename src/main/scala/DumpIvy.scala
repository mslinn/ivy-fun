import org.apache.ivy.Ivy
import org.apache.ivy.core.repository.RepositoryManagementEngine
import org.apache.ivy.plugins.resolver.DependencyResolver

class DumpIvy(implicit ivy: Ivy) {
  dumpIvy

  /** This is supposed to show all the modules in the Ivy cache. Unfortunately it does not do anything */
  def dumpIvy(implicit ivy: Ivy): Unit = {
    val repoEngine: RepositoryManagementEngine = ivy.getRepositoryEngine
    repoEngine.load()

    ivy.listOrganisationEntries.foreach { orgEntry =>
      println(s"orgEntry.getOrganisation = ${orgEntry.getOrganisation};")

      val resolver: DependencyResolver = orgEntry.getResolver
      println(resolver)

      ivy.listModuleEntries(orgEntry).foreach { moduleEntry =>
        println(s"moduleEntry.getModule =  ${moduleEntry.getModule}")
        ivy.listModules(orgEntry.getOrganisation).foreach { module =>
          println(s"module = $module")
        }
      }
    }
  }
}

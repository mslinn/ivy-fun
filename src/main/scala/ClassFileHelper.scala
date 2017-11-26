import java.net.URL

class ClassFileHelper {
  def getClassUrl(clazz: Class[_] ): URL = clazz.getResource('/' + clazz.getName.replace('.', '/') + ".class")

  def getJarFromUrl(url: URL): String = {
    assert(url.getProtocol == "jar")
    val fileName = url.getFile
    val bang = fileName.substring(0, fileName.lastIndexOf('!'))
    bang.substring(fileName.lastIndexOf('/') + 1)
  }
}

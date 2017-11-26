import java.util.jar
import org.apache.ivy.Ivy

object Main extends App {
  val test = new Test
}

/** Given a list of paths to jars of the form: /home/mslinn/.sbt/boot/scala-2.12.4/lib/scala-library.jar,
  * discover their javadoc/scaladoc urls from information in the Jar manifest.
  * Return a stringified tuple (using # as a delimiter) that contains the path to the jar and the libraries' documentation URL.
  * For example:
  *   /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar#http://docs.oracle.com/javase/8/docs/api/
  *
  * A list of these tuples will be used as a value for Scaladoc's -doc-external-doc command line option */
class Test {
  implicit val ivy: Ivy = Ivy.newInstance()

  new ThirdTry

//  val x: jar.Manifest = JarManifest.manifest
//  println(x)
}

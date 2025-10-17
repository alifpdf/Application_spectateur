error id: 046B01E7E3DF6E9E826DC8A89D81964C
file://<WORKSPACE>/server/src/fr/cyu/robafis/MinimalApplication.scala
### java.lang.IndexOutOfBoundsException: 1

occurred in the presentation compiler.



action parameters:
offset: 1911
uri: file://<WORKSPACE>/server/src/fr/cyu/robafis/MinimalApplication.scala
text:
```scala
package fr.cyu.robafis

import upickle.*
import cask.model.StaticResource
import cask.model.Response
import scala.concurrent.blocking
import scala.concurrent.Future

object MinimalApplication extends cask.MainRoutes:

  var model = Model(0, Map.empty, 0,3,3)

  val password = "abc"

  override def host: String = "0.0.0.0"
  override def port: Int = 8080

  @cask.staticResources("/static")
  def staticResourceRoutes() = "."

  @cask.get("/")
  def indexSpec() = Response(
    data = os.read(os.resource / "index.html"),
    headers = Seq("Content-Type" -> "text/html")
  )

  @cask.get("/coach")
  def indexCoach() = indexSpec()

  @cask.websocket("/connect")
  def specWS(): cask.WebsocketResult =
    cask.WsHandler( channel =>
      val res = model.withNewSession(channel)
      val id = res._1
      model = res._2

      channel.send(cask.Ws.Text(upickle.write(ServerMsg.SetCount(model.counter))))

      cask.WsActor:
        case cask.Ws.Text(json) =>
          val coachMsg = upickle.read[CoachMsg](json)
          coachMsg match
            case CoachMsg.Login(password) =>
              if password == this.password then
                println(s"Authentication success for session $id")
                model = model.promote(id)
                channel.send(cask.Ws.Text(upickle.write(ServerMsg.LoggedIn)))
              else
                println(s"Authentication failed for session $id")
            
            case CoachMsg.Incr(n) =>
              println(s"Increment $n (from session $id)")
              if model.isCoach(id) then
                model = model.copy(counter = model.counter + n)
                model.sendToAll(ServerMsg.SetCount(model.counter))
            case CoachMsg.Reset =>
              if model.isCoach(id) then
                model = model.copy(counter=0)
                model.sendToAll(ServerMsg.SetCount(model.counter))
            case CoachMsg.Coor(row, co@@) => 
              if model.isCoach(id) then
                model =model.copy(r = r,c=c)
    )

  initialize()
```


presentation compiler configuration:
Scala version: 3.7.3-bin-nonbootstrapped
Classpath:
<WORKSPACE>/out/mill-bsp-out/server/compiledClassesAndSemanticDbFiles.dest [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_3/3.7.3/scala3-library_3-3.7.3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/cask_3/0.10.2/cask_3-0.10.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-lib_3/0.11.5/os-lib_3-0.11.5.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_3/4.3.2/upickle_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/undertow/undertow-core/2.3.18.Final/undertow-core-2.3.18.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/cask-util_3/0.10.2/cask-util_3-0.10.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_3/1.1.1/geny_3-1.1.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/os-zip/0.11.5/os-zip-0.11.5.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_3/4.3.2/ujson_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_3/4.3.2/upack_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_3/4.3.2/upickle-implicits_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jboss/xnio/xnio-api/3.8.16.Final/xnio-api-3.8.16.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jboss/threads/jboss-threads/3.5.0.Final/jboss-threads-3.5.0.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/sourcecode_3/0.4.2/sourcecode_3-0.4.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/pprint_3/0.9.0/pprint_3-0.9.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/castor_3/0.3.0/castor_3-0.3.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.5.3/Java-WebSocket-1.5.3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_3/4.3.2/upickle-core_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/wildfly/common/wildfly-common/1.6.0.Final/wildfly-common-1.6.0.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/wildfly/client/wildfly-client-config/1.0.1.Final/wildfly-client-config-1.0.1.Final.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/fansi_3/0.5.0/fansi_3-0.5.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar [exists ], <WORKSPACE>/common/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/compile.dest/classes [exists ], <WORKSPACE>/server/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ], <WORKSPACE>/out/mill-bsp-out/server/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ]
Options:
-Ywith-best-effort-tasty




#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.pc.InferCompletionType$.inferType(InferExpectedType.scala:94)
	dotty.tools.pc.InferCompletionType$.inferType(InferExpectedType.scala:62)
	dotty.tools.pc.completions.Completions.advancedCompletions(Completions.scala:523)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:122)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:139)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:197)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 1
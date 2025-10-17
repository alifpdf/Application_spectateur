error id: FBFC828E2E46293FFF97C9454409B193
file://<WORKSPACE>/client/src/fr/cyu/robafis/Msg.scala
### scala.MatchError: val <none> (of class dotty.tools.dotc.core.Symbols$NoSymbol$)

occurred in the presentation compiler.



action parameters:
offset: 282
uri: file://<WORKSPACE>/client/src/fr/cyu/robafis/Msg.scala
text:
```scala
package fr.cyu.robafis

import tyrian.websocket.WebSocket
import cats.effect.IO


enum Msg:
  case NoOp
  case Connect
  case Connected(socket: WebSocket[IO])
  case Send(msg: CoachMsg)
  case Receive(msg: ServerMsg)
  case CoachView
  case UpdatePasswordText(text: String)


enum M@@sg:
  case StartScan
    case DeviceListUpdated(devs: List[BtDevice])
    case SelectDevice(id: String)
    case ConnectSelected
    case Connectedbdevice(id: String)
    case Disconnect(id: String)
    case Disconnected(id: String)
    case Error(msg: String)
```


presentation compiler configuration:
Scala version: 3.7.3-bin-nonbootstrapped
Classpath:
<WORKSPACE>/out/mill-bsp-out/client/compiledClassesAndSemanticDbFiles.dest [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_sjs1_3/3.7.3/scala3-library_sjs1_3-3.7.3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-library_2.13/1.20.1/scalajs-library_2.13-1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian_sjs1_3/0.14.0/tyrian_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian-io_sjs1_3/0.14.0/tyrian-io_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_sjs1_3/4.3.2/upickle_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-javalib/1.20.1/scalajs-javalib-1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-scalalib_2.13/2.13.16%2B1.20.1/scalajs-scalalib_2.13-2.13.16%2B1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian-tags_sjs1_3/0.14.0/tyrian-tags_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect-kernel_sjs1_3/3.6.1/cats-effect-kernel_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/co/fs2/fs2-core_sjs1_3/3.12.0/fs2-core_sjs1_3-3.12.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/github/buntec/scala-js-snabbdom_sjs1_3/0.2.0-M3/scala-js-snabbdom_sjs1_3-0.2.0-M3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-dom_sjs1_3/2.8.0/scalajs-dom_sjs1_3-2.8.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect_sjs1_3/3.6.1/cats-effect_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_sjs1_3/4.3.2/ujson_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_sjs1_3/4.3.2/upack_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_sjs1_3/4.3.2/upickle-implicits_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-core_sjs1_3/2.11.0/cats-core_sjs1_3-2.11.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scodec/scodec-bits_sjs1_3/1.1.38/scodec-bits_sjs1_3-1.1.38.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect-std_sjs1_3/3.6.1/cats-effect-std_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-mtl_sjs1_3/1.3.1/cats-mtl_sjs1_3-1.3.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scala-js-macrotask-executor_sjs1_3/1.1.1/scala-js-macrotask-executor_sjs1_3-1.1.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_sjs1_3/4.3.2/upickle-core_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-kernel_sjs1_3/2.11.0/cats-kernel_sjs1_3-2.11.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_sjs1_3/1.1.1/geny_sjs1_3-1.1.1.jar [exists ], <WORKSPACE>/common/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/js/compile.dest/classes [exists ], <WORKSPACE>/client/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/js/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ], <WORKSPACE>/out/mill-bsp-out/client/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ]
Options:
-scalajs -Ywith-best-effort-tasty




#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$ClassDenotation.computeMemberNames$$anonfun$1(SymDenotations.scala:2401)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.core.SymDenotations$ClassDenotation.computeMemberNames(SymDenotations.scala:2397)
	dotty.tools.dotc.core.SymDenotations$MemberNamesImpl.apply(SymDenotations.scala:2997)
	dotty.tools.dotc.core.SymDenotations$ClassDenotation.memberNames(SymDenotations.scala:2390)
	dotty.tools.dotc.core.Types$Type.memberNames(Types.scala:1033)
	dotty.tools.dotc.core.Types$Type.memberDenots(Types.scala:1050)
	dotty.tools.dotc.core.Types$Type.implicitMembers(Types.scala:1135)
	dotty.tools.dotc.typer.Typer.implementDeferredGivens$1(Typer.scala:3294)
	dotty.tools.dotc.typer.Typer.typedClassDef(Typer.scala:3328)
	dotty.tools.dotc.typer.Typer.typedTypeOrClassDef$1(Typer.scala:3659)
	dotty.tools.dotc.typer.Typer.typedNamed$1(Typer.scala:3663)
	dotty.tools.dotc.typer.Typer.typedUnadapted(Typer.scala:3758)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3836)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3841)
	dotty.tools.dotc.typer.Typer.traverse$1(Typer.scala:3863)
	dotty.tools.dotc.typer.Typer.typedStats(Typer.scala:3909)
	dotty.tools.dotc.typer.Typer.typedClassDef(Typer.scala:3328)
	dotty.tools.dotc.typer.Typer.typedTypeOrClassDef$1(Typer.scala:3659)
	dotty.tools.dotc.typer.Typer.typedNamed$1(Typer.scala:3663)
	dotty.tools.dotc.typer.Typer.typedUnadapted(Typer.scala:3758)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3836)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3841)
	dotty.tools.dotc.typer.Typer.traverse$1(Typer.scala:3863)
	dotty.tools.dotc.typer.Typer.typedStats(Typer.scala:3909)
	dotty.tools.dotc.typer.Typer.typedPackageDef(Typer.scala:3461)
	dotty.tools.dotc.typer.Typer.typedUnnamed$1(Typer.scala:3705)
	dotty.tools.dotc.typer.Typer.typedUnadapted(Typer.scala:3759)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3836)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3841)
	dotty.tools.dotc.typer.Typer.typedExpr(Typer.scala:3952)
	dotty.tools.dotc.typer.TyperPhase.typeCheck$$anonfun$1(TyperPhase.scala:47)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	dotty.tools.dotc.core.Phases$Phase.monitor(Phases.scala:510)
	dotty.tools.dotc.typer.TyperPhase.typeCheck(TyperPhase.scala:53)
	dotty.tools.dotc.typer.TyperPhase.$anonfun$4(TyperPhase.scala:99)
	scala.collection.Iterator$$anon$6.hasNext(Iterator.scala:479)
	scala.collection.Iterator$$anon$9.hasNext(Iterator.scala:583)
	scala.collection.immutable.List.prependedAll(List.scala:152)
	scala.collection.immutable.List$.from(List.scala:685)
	scala.collection.immutable.List$.from(List.scala:682)
	scala.collection.IterableOps$WithFilter.map(Iterable.scala:900)
	dotty.tools.dotc.typer.TyperPhase.runOn(TyperPhase.scala:98)
	dotty.tools.dotc.Run.runPhases$1$$anonfun$1(Run.scala:380)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.ArrayOps$.foreach$extension(ArrayOps.scala:1324)
	dotty.tools.dotc.Run.runPhases$1(Run.scala:373)
	dotty.tools.dotc.Run.compileUnits$$anonfun$1$$anonfun$2(Run.scala:420)
	dotty.tools.dotc.Run.compileUnits$$anonfun$1$$anonfun$adapted$1(Run.scala:420)
	scala.Function0.apply$mcV$sp(Function0.scala:42)
	dotty.tools.dotc.Run.showProgress(Run.scala:482)
	dotty.tools.dotc.Run.compileUnits$$anonfun$1(Run.scala:420)
	dotty.tools.dotc.Run.compileUnits$$anonfun$adapted$1(Run.scala:432)
	dotty.tools.dotc.util.Stats$.maybeMonitored(Stats.scala:69)
	dotty.tools.dotc.Run.compileUnits(Run.scala:432)
	dotty.tools.dotc.Run.compileSources(Run.scala:319)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:161)
	dotty.tools.pc.CachingDriver.run(CachingDriver.scala:45)
	dotty.tools.pc.HoverProvider$.hover(HoverProvider.scala:43)
	dotty.tools.pc.ScalaPresentationCompiler.hover$$anonfun$1(ScalaPresentationCompiler.scala:452)
```
#### Short summary: 

scala.MatchError: val <none> (of class dotty.tools.dotc.core.Symbols$NoSymbol$)
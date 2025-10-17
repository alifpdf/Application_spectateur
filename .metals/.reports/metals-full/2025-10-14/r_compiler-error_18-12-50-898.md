error id: A4925103C6910D863D9C87202B953C93
### dotty.tools.dotc.MissingCoreLibraryException: Could not find package scala from compiler core libraries.
Make sure the compiler core libraries are on the classpath.
   

occurred in the presentation compiler.



action parameters:
<NONE>

presentation compiler configuration:
Scala version: 3.7.3-bin-nonbootstrapped
Classpath:
<WORKSPACE>/out/mill-bsp-out/server/compiledClassesAndSemanticDbFiles.dest [exists ], <WORKSPACE>/out/mill-bsp-out/common/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ], <WORKSPACE>/out/mill-bsp-out/server/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ]
Options:
-Ywith-best-effort-tasty




#### Error stacktrace:

```
dotty.tools.dotc.core.Denotations$.select$1(Denotations.scala:1325)
	dotty.tools.dotc.core.Denotations$.recurSimple$1(Denotations.scala:1353)
	dotty.tools.dotc.core.Denotations$.recur$1(Denotations.scala:1355)
	dotty.tools.dotc.core.Denotations$.staticRef(Denotations.scala:1359)
	dotty.tools.dotc.core.Symbols$.requiredPackage(Symbols.scala:1010)
	dotty.tools.dotc.core.Definitions.ScalaPackageVal(Definitions.scala:215)
	dotty.tools.dotc.core.Definitions.ScalaPackageClass(Definitions.scala:218)
	dotty.tools.dotc.core.Definitions.AnyClass(Definitions.scala:282)
	dotty.tools.dotc.core.Definitions.syntheticScalaClasses(Definitions.scala:2243)
	dotty.tools.dotc.core.Definitions.syntheticCoreClasses(Definitions.scala:2258)
	dotty.tools.dotc.core.Definitions.init(Definitions.scala:2274)
	dotty.tools.dotc.core.Contexts$ContextBase.initialize(Contexts.scala:934)
	dotty.tools.dotc.core.Contexts$Context.initialize(Contexts.scala:546)
	dotty.tools.dotc.interactive.InteractiveDriver.<init>(InteractiveDriver.scala:41)
	dotty.tools.pc.CachingDriver.<init>(CachingDriver.scala:30)
	dotty.tools.pc.ScalaPresentationCompiler.$init$$$anonfun$1(ScalaPresentationCompiler.scala:132)
```
#### Short summary: 

dotty.tools.dotc.MissingCoreLibraryException: Could not find package scala from compiler core libraries.
Make sure the compiler core libraries are on the classpath.
   
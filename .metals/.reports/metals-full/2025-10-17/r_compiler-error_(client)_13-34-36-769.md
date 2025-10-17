error id: 046B01E7E3DF6E9E826DC8A89D81964C
file://<WORKSPACE>/client/src/fr/cyu/robafis/Main.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.



action parameters:
offset: 2620
uri: file://<WORKSPACE>/client/src/fr/cyu/robafis/Main.scala
text:
```scala
package fr.cyu.robafis

import cats.effect.IO
import tyrian.Html.*
import tyrian.*
import tyrian.Attr
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import tyrian.websocket.{WebSocket, WebSocketConnect, WebSocketEvent}
import upickle.default.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  // ---------- Helper style (évite l'ambiguïté "style") ----------
  private inline def css(s: String): Attr[Msg] =
    attr("style") := s

  // ---------- Styles de boutons ----------
  private val baseBtn =
    "display:inline-flex; align-items:center; justify-content:center;" +
      "height:38px; padding:0 12px; border-radius:10px; border:1px solid transparent;" +
      "font-size:14px; cursor:pointer; user-select:none; transition:all .15s ease;"

  private val primaryBtn = baseBtn + "background:#2563eb; color:#fff; border-color:#2563eb;"
  private val successBtn = baseBtn + "background:#16a34a; color:#fff; border-color:#16a34a;"
  private val warnBtn    = baseBtn + "background:#f59e0b; color:#111827; border-color:#f59e0b;"
  private val dangerBtn  = baseBtn + "background:#ef4444; color:#fff; border-color:#ef4444;"
  private val outlineBtn = baseBtn + "background:#fff; color:#111827; border-color:#c7cdd4;"
  private val mutedBtn   = baseBtn + "background:#f3f4f6; color:#9ca3af; border-color:#e5e7eb; cursor:not-allowed;"

  // ---------- Router ----------
  def router: Location => Msg =
    case loc: Location.Internal =>
      if loc.pathName == "/coach" then Msg.CoachView else Msg.NoOp
    case _: Location.External => Msg.NoOp

  // ---------- Init ----------
  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.empty, Cmd.emit(Msg.Connect))

  // ---------- Update ----------
  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    {
      case Msg.NoOp =>
        (model, Cmd.None)

      // WebSocket
      case Msg.Connect =>
        (
          model,
          WebSocket.connect[IO, Msg]("/connect") {
            case WebSocketConnect.Error(error) =>
              println(s"ERROR: $error"); Msg.NoOp
            case WebSocketConnect.Socket(socket) =>
              Msg.Connected(socket)
          }
        )

      case Msg.Connected(socket) =>
        (model.copy(socket = Some(socket)), Cmd.None)

      case Msg.Send(msg) =>
        (model, model.socket.fold(Cmd.None)(_.publish(write(msg))))

      case Msg.Receive(ServerMsg.SetCount(n)) =>
        (model.copy(counter = n), Cmd.None)

      case Msg.Receive(ServerMsg.LoggedIn) =>
          (model.copy(isLoggedIn = true), Cmd.None)
      
      case Msg.Receive(r@@) => 

      case Msg.CoachView =>
        (model.copy(isCoachView = true), Cmd.None)

      case Msg.UpdatePasswordText(text) =>
        (model.copy(coachPassword = text), Cmd.None)

      // ---------- Bluetooth ----------
      case Msg.Bluetooth(BluetoothMsg.StartScan) =>
        val effect: IO[Msg] =
          IO.fromFuture(IO(BluetoothJS.requestMakeblockDevice()))
            .attempt
            .map {
              case Right(dev) =>
                val ok = dev.name.toOption.exists(_.toLowerCase.startsWith("makeblock"))
                if !ok then
                  Msg.Bluetooth(BluetoothMsg.Error("Aucun appareil Makeblock trouvé"))
                else
                  Msg.Bluetooth(BluetoothMsg.DeviceListUpdated(
                    List(BtDevice(dev.id, dev.name.getOrElse("Inconnu"), connected = false))
                  ))
              case Left(err) =>
                Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
            }
        val m2 = model.copy(bt = model.bt.copy(scanning = true, error = None))
        (m2, Cmd.Run(effect))

      case Msg.Bluetooth(BluetoothMsg.DeviceListUpdated(devs)) =>
        (model.copy(bt = model.bt.copy(scanning = false, devices = devs, error = None)), Cmd.None)

      case Msg.Bluetooth(BluetoothMsg.SelectDevice(id)) =>
        (model.copy(bt = model.bt.copy(selectedId = Some(id), error = None)), Cmd.None)

      case Msg.Bluetooth(BluetoothMsg.ConnectSelected) =>
        model.bt.selectedId match
          case None =>
            (model, Cmd.emit(Msg.Bluetooth(BluetoothMsg.Error("Aucun appareil sélectionné"))))
          case Some(id) =>
            val io: IO[Msg] =
              IO.fromFuture(IO(BluetoothJS.connectById(id))).attempt.map {
                case Right(okId) => Msg.Bluetooth(BluetoothMsg.Connected(okId))
                case Left(err)   => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
              }
            (model, Cmd.Run(io))

      case Msg.Bluetooth(BluetoothMsg.Connected(id)) =>
        val updated = model.bt.devices.map(d => if d.id == id then d.copy(connected = true) else d)
        val io: IO[Msg] =
          IO.fromFuture(IO(BluetoothJS.enableNotifyById(id))).attempt.map {
            case Right(_)  => Msg.NoOp
            case Left(err) => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
          }
        (model.copy(bt = model.bt.copy(devices = updated, error = None)), Cmd.Run(io))

      case Msg.Bluetooth(BluetoothMsg.Disconnect(id)) =>
        val io: IO[Msg] =
          IO.fromFuture(IO(BluetoothJS.disconnectById(id))).attempt.map {
            case Right(okId) => Msg.Bluetooth(BluetoothMsg.Disconnected(okId))
            case Left(err)   => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
          }
        (model, Cmd.Run(io))

      case Msg.Bluetooth(BluetoothMsg.Disconnected(id)) =>
        val updated = model.bt.devices.map(d => if d.id == id then d.copy(connected = false) else d)
        (model.copy(bt = model.bt.copy(devices = updated, error = None)), Cmd.None)

      case Msg.Bluetooth(BluetoothMsg.WriteToSelected(payload)) =>
        model.bt.selectedId match
          case None =>
            (model, Cmd.emit(Msg.Bluetooth(BluetoothMsg.Error("Aucun appareil sélectionné"))))
          case Some(id) =>
            val io: IO[Msg] =
              IO.fromFuture(IO(BluetoothJS.writeTextById(id, payload))).attempt.map {
                case Right(_)   => Msg.Bluetooth(BluetoothMsg.Wrote(payload))
                case Left(err)  => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
              }
            (model, Cmd.Run(io))

      case Msg.Bluetooth(BluetoothMsg.Wrote(_)) =>
        (model, Cmd.None)

      case Msg.Bluetooth(BluetoothMsg.EnableNotifySelected) =>
        model.bt.selectedId match
          case None    => (model, Cmd.emit(Msg.Bluetooth(BluetoothMsg.Error("Aucun appareil sélectionné"))))
          case Some(id) =>
            val io: IO[Msg] =
              IO.fromFuture(IO(BluetoothJS.enableNotifyById(id))).attempt.map {
                case Right(_)  => Msg.NoOp
                case Left(err) => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
              }
            (model, Cmd.Run(io))

      // ---------- Réception notifications BLE ----------
      case Msg.Bluetooth(BluetoothMsg.NotificationReceived(txt)) =>
        val cleaned  = txt.replace("\r\n", "\n").replace('\r', '\n')
        val combined = model.bt.buffer + cleaned
        val parts    = combined.split("\n", -1).toList

        val (completeLines, newBuf) =
          if combined.endsWith("\n") then (parts.dropRight(1), "")
          else (parts.dropRight(1), parts.last)

        // Log limité à 500 lignes
        val newLog = (model.bt.notifications ++ completeLines.filter(_.nonEmpty)).takeRight(500)

        // --- Extraction de la dernière position POS r c ---
        val Pos = raw"""\bPOS\s+(\d+)\s+(\d+)\b""".r
        val lastPosOpt: Option[(Int, Int)] =
          completeLines.collect {
            case Pos(r, c) =>
              val rr = math.max(1, math.min(5, r.toInt))
              val cc = math.max(1, math.min(4, c.toInt))
              (rr, cc)
          }.lastOption

     
        // --- Extraction du dernier POINT n ---
        val Point5 = raw"""\bPOINT5\s+(\d+)\b""".r
        val lastPointOpt5: Option[Int] =
          completeLines.collect { case Point5(n) => n.toInt }.lastOption

        // --- Commande Tyrian pour envoyer CoachMsg.Incr(n) ---
        val incrCmd5: Cmd[IO, Msg] =
          lastPointOpt5
            .map(n => Cmd.Emit(Msg.Send(CoachMsg.Incr(n))))
            .getOrElse(Cmd.None)

        val Point2 = raw"""\bPOINT2\s+(\d+)\b""".r
        val lastPointOpt2: Option[Int] =
          completeLines.collect { case Point2(n) => n.toInt }.lastOption

        // --- Commande Tyrian pour envoyer CoachMsg.Incr(n) ---
        val incrCmd2: Cmd[IO, Msg] =
          lastPointOpt2
            .map(n => Cmd.Emit(Msg.Send(CoachMsg.Incr(n))))
            .getOrElse(Cmd.None)

            


        val m2 = model.copy(bt = model.bt.copy(
          notifications = newLog,
          buffer = newBuf,
          active = lastPosOpt.orElse(model.bt.active)
        ))

        val scrollCmd: Cmd[IO, Msg] = Cmd.Run(
          IO {
            Option(dom.document.getElementById("bt-log")).foreach { el =>
              val dyn = el.asInstanceOf[scala.scalajs.js.Dynamic]
              dyn.scrollTop = dyn.scrollHeight.asInstanceOf[Double]
            }
            Msg.NoOp
          }
        )

        // --- Combinaison avec ton scrollCmd existant ---
        val combinedCmd = Cmd.Batch(scrollCmd, incrCmd5,incrCmd2)

        (m2, combinedCmd)

      case Msg.Bluetooth(BluetoothMsg.Error(e)) =>
        (model.copy(bt = model.bt.copy(scanning = false, error = Some(e))), Cmd.None)

      case Msg.Bluetooth(_) =>
        (model, Cmd.None)
    }

  // ---------- Menu Bluetooth ----------
  private def bluetoothMenu(model: Model): Html[Msg] =
    details(
      summary("Bluetooth (Makeblock)"),
      div(
        p(
          if model.bt.scanning then "Recherche en cours…"
          else "Sélectionne un appareil Makeblock puis connecte-toi."
        ),
        div(
          button(onClick(Msg.Bluetooth(BluetoothMsg.StartScan)))("Rechercher des appareils Makeblock"),
          text(" "),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.ConnectSelected)),
            disabled(model.bt.selectedId.isEmpty)
          )("Se connecter")
        ),
        div(
          label(attr("for") := "bt-device-select")("Appareils : "),
          select(
            id := "bt-device-select",
            onChange(v => Msg.Bluetooth(BluetoothMsg.SelectDevice(v)))
          )(
            option(value := "", selected(model.bt.selectedId.isEmpty))("— choisir —") ::
              model.bt.devices.map { d =>
                option(
                  value := d.id,
                  selected(model.bt.selectedId.contains(d.id))
                )(s"${d.name} ${if d.connected then "(connecté)" else ""}")
              }
          )
        ),
        ul(
          model.bt.devices.map { d =>
            li(
              span(s"${d.name} [${d.id}] "),
              if d.connected then
                button(onClick(Msg.Bluetooth(BluetoothMsg.Disconnect(d.id))))("Se déconnecter")
              else
                div(
                  button(onClick(Msg.Bluetooth(BluetoothMsg.SelectDevice(d.id))))("Sélectionner"),
                  text(" "),
                  button(onClick(Msg.Bluetooth(BluetoothMsg.ConnectSelected)))("Se connecter")
                )
            )
          }
        ),
        model.bt.error.fold[Html[Msg]](span())(err => p(css("color:red;"))(err))
      )
    )

  // ---------- Vue "terrain simple" : 4 colonnes x 5 lignes, une case active ----------
  private def fieldView(model: Model): Html[Msg] =
    val activeOpt: Option[(Int, Int)] = model.bt.active  // (row, col) 1..5, 1..4

    val rows = 5
    val cols = 4

    val cells: List[Html[Msg]] =
      (1 to rows).toList.flatMap { r =>
        (1 to cols).toList.map { c =>
          val isActive = activeOpt.contains((r, c))
          val styleBase =
            "display:flex; align-items:center; justify-content:center;" +
            "border:1px solid #d1d5db; border-radius:8px;" +
            "font-size:14px; user-select:none;"

          val styleColor =
            if isActive then "background:#22c55e; color:#fff; font-weight:700;"
            else "background:#eef2f7; color:#374151;"

          div(css(styleBase + styleColor))(s"R$r-C$c")
        }
      }

    div(css("background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; box-shadow:0 1px 2px rgba(0,0,0,.03);"))(
      h2(css("margin:0 0 12px; font-size:16px;"))("Terrain (26 cm × 18 cm)"),
      p(css("margin:0 0 10px; color:#6b7280; font-size:12px;"))(
        text("Envoyez depuis l’Arduino : "),
        code("POS <row> <col>"),
        text(" (ex : "),
        code("POS 3 2"),
        text("). Lignes 1..5, Colonnes 1..4.")
      ),
      div(
        css(
          "display:grid; grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(5, 64px);" +
          "gap:10px; width:100%; max-width:700px; aspect-ratio: 26 / 18;"
        )
      )(
        cells*
      )
    )

  // ---------- Vue principale connectée ----------
  def loggedInView(model: Model): Html[Msg] =
    div(
      css(
        "max-width: 980px; margin: 20px auto; padding: 16px;" +
          "font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;"
      )
    )(
      // Header
      div(css("display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:16px;"))(
        div(
          h1(css("font-size: 22px; margin: 0; letter-spacing: .2px;"))("Coach • Contrôle matériel"),
          p(css("margin: 6px 0 0; color: #666; font-size: 13px;"))("Pilote Makeblock via BLE + WebSocket")
        ),
        span(
          css(
            "padding:6px 10px; border-radius:999px; font-size:12px; " +
              (if model.bt.selectedId.isEmpty
               then "background:#fff3cd; color:#664d03; border:1px solid #ffe69c;"
               else "background:#e7f5ff; color:#0b7285; border:1px solid #a5d8ff;")
          )
        )(
          if model.bt.selectedId.isEmpty then "Aucun appareil sélectionné" else "Appareil sélectionné"
        )
      ),

      // Carte : Commandes rapides
      div(css("background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; box-shadow:0 1px 2px rgba(0,0,0,.03); margin-bottom:14px;"))(
        h2(css("margin:0 0 12px; font-size:16px;"))("Commandes rapides"),
        div(css("display:grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap:10px;"))(
          button(onClick(Msg.Send(CoachMsg.Incr(2))),  css(primaryBtn))("+2"),
          button(onClick(Msg.Send(CoachMsg.Incr(5))),  css(primaryBtn))("+5"),
          button(onClick(Msg.Send(CoachMsg.Reset)),    css(dangerBtn)) ("Reset"),

          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("1"))),
            disabled(model.bt.selectedId.isEmpty),
            css(if model.bt.selectedId.isEmpty then mutedBtn else successBtn)
          )("Envoyer 1"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("0"))),
            disabled(model.bt.selectedId.isEmpty),
            css(if model.bt.selectedId.isEmpty then mutedBtn else warnBtn)
          )("Envoyer 0"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("2"))),
            disabled(model.bt.selectedId.isEmpty),
            title := "Lancer les tests matériels (commande '2')",
            css(if model.bt.selectedId.isEmpty then mutedBtn else primaryBtn)
          )("Tests (2)"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.EnableNotifySelected)),
            disabled(model.bt.selectedId.isEmpty),
            css(if model.bt.selectedId.isEmpty then mutedBtn else outlineBtn)
          )("Activer notifications")
        )
      ),

      // Carte : Bluetooth
      div(css("background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; box-shadow:0 1px 2px rgba(0,0,0,.03); margin-bottom:14px;"))(
        h2(css("margin:0 0 12px; font-size:16px;"))("Bluetooth"),
        bluetoothMenu(model)
      ),

      // Carte : Log
      div(css("background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; box-shadow:0 1px 2px rgba(0,0,0,.03);"))(
        div(css("display:flex; align-items:center; justify-content:space-between; margin-bottom:10px;"))(
          h2(css("margin:0; font-size:16px;"))("Retours du module"),
          span(css("font-size:12px; color:#6b7280;"))(s"${model.bt.notifications.length} ligne(s)")
        ),
        div(
          id := "bt-log",
          css(
            "height: 420px; max-height: 55vh; overflow-y: auto;" +
              "border: 1px dashed #d1d5db; border-radius: 10px; padding: 10px;" +
              "font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;" +
              "white-space: pre-wrap; line-height: 1.28; background: #f9fafb;"
          )
        )(
          pre(css("margin:0;"))(model.bt.notifications.mkString("\n"))
        ),
        model.bt.error.fold[Html[Msg]](div())(err =>
          div(css("margin-top:10px; font-size: 13px; padding:8px 10px; border-radius:8px; background:#fff5f5; color:#9f1239; border:1px solid #fecaca;"))(
            s"Erreur Bluetooth : $err"
          )
        )
      )


    )

  // ---------- Vue mot de passe ----------
  def passwordView(model: Model): Html[Msg] =
    div(css("max-width: 420px; margin: 60px auto; padding: 20px; font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;"))(
      div(css("background:#fff; border:1px solid #e5e7eb; border-radius:14px; padding:20px; box-shadow: 0 1px 2px rgba(0,0,0,.04);"))(
        h2(css("margin:0 0 10px; font-size:18px;"))("Accès coach"),
        p(css("margin:0 0 14px; color:#6b7280; font-size:13px;"))(
          text("Saisissez le mot de passe pour débloquer les commandes avancées.")
        ),
        div(
          label(css("display:block; font-size:13px; margin: 0 0 6px;"))("Mot de passe"),
          input(
            tpe := "password",
            onInput(Msg.UpdatePasswordText.apply),
            css("width:100%; height:40px; padding:0 10px; border-radius:10px; border:1px solid #d1d5db; outline:none;")
          )
        ),
        div(css("height:10px"))(""),
        button(onClick(Msg.Send(CoachMsg.Login(model.coachPassword))), css(primaryBtn + "width:100%;"))("Se connecter")
      )
    )

  // ---------- View ----------
  def view(model: Model): Html[Msg] =
    div(
      css("min-height:100vh; background:#f5f7fb;")
    )(
      // Barre supérieure avec compteur
      div(css("max-width:980px; margin:16px auto 0; padding:0 16px; display:flex; justify-content:flex-end;"))(
        span(css("font-size:12px; padding:6px 10px; border-radius:999px; background:#eef2ff; color:#3730a3; border:1px solid #c7d2fe;"))(
          s"Compteur : ${model.counter}"
        )
      ),
      if model.isLoggedIn then
        loggedInView(model)
      else if model.shouldAskPassword then
        passwordView(model)
      else
        div(css("max-width: 980px; margin: 40px auto; padding: 0 16px; color:#6b7280; font-size:14px;"))(
          text("Bienvenue — sélectionnez un appareil Bluetooth pour commencer.")
        ),
       // ▼▼▼ Terrain simple sous les retours Arduino ▼▼▼
      div(css("height:16px"))(""),
      fieldView(model)
    )

  // ---------- Subscriptions ----------
  def subscriptions(model: Model): Sub[IO, Msg] =
    val wsSub: Sub[IO, Msg] =
      model.socket.fold(Sub.None)(_.subscribe:
        case WebSocketEvent.Heartbeat        => Msg.NoOp
        case WebSocketEvent.Open             => Msg.NoOp
        case WebSocketEvent.Close(_, _)      => Msg.NoOp
        case WebSocketEvent.Error(error)     => println(s"ERROR event: $error"); Msg.NoOp
        case WebSocketEvent.Receive(message) => Msg.Receive(read[ServerMsg](message))
      )

    val btNotifySub: Sub[IO, Msg] =
      Sub.fromEvent[IO, dom.CustomEvent, Msg](
        "bt-notify",
        dom.window
      ) { ce =>
        val detail = Option(ce.detail).map(_.toString).getOrElse("")
        Some(Msg.Bluetooth(BluetoothMsg.NotificationReceived(detail)))
      }

    Sub.Batch(wsSub, btNotifySub)

```


presentation compiler configuration:
Scala version: 3.7.3-bin-nonbootstrapped
Classpath:
<WORKSPACE>/out/mill-bsp-out/client/compiledClassesAndSemanticDbFiles.dest [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_sjs1_3/3.7.3/scala3-library_sjs1_3-3.7.3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-library_2.13/1.20.1/scalajs-library_2.13-1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian_sjs1_3/0.14.0/tyrian_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian-io_sjs1_3/0.14.0/tyrian-io_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle_sjs1_3/4.3.2/upickle_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-javalib/1.20.1/scalajs-javalib-1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-scalalib_2.13/2.13.16%2B1.20.1/scalajs-scalalib_2.13-2.13.16%2B1.20.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/indigoengine/tyrian-tags_sjs1_3/0.14.0/tyrian-tags_sjs1_3-0.14.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect-kernel_sjs1_3/3.6.1/cats-effect-kernel_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/co/fs2/fs2-core_sjs1_3/3.12.0/fs2-core_sjs1_3-3.12.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/io/github/buntec/scala-js-snabbdom_sjs1_3/0.2.0-M3/scala-js-snabbdom_sjs1_3-0.2.0-M3.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scalajs-dom_sjs1_3/2.8.0/scalajs-dom_sjs1_3-2.8.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect_sjs1_3/3.6.1/cats-effect_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/ujson_sjs1_3/4.3.2/ujson_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upack_sjs1_3/4.3.2/upack_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-implicits_sjs1_3/4.3.2/upickle-implicits_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-core_sjs1_3/2.11.0/cats-core_sjs1_3-2.11.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scodec/scodec-bits_sjs1_3/1.1.38/scodec-bits_sjs1_3-1.1.38.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect-std_sjs1_3/3.6.1/cats-effect-std_sjs1_3-3.6.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-mtl_sjs1_3/1.3.1/cats-mtl_sjs1_3-1.3.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-js/scala-js-macrotask-executor_sjs1_3/1.1.1/scala-js-macrotask-executor_sjs1_3-1.1.1.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/upickle-core_sjs1_3/4.3.2/upickle-core_sjs1_3-4.3.2.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-kernel_sjs1_3/2.11.0/cats-kernel_sjs1_3-2.11.0.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/geny_sjs1_3/1.1.1/geny_sjs1_3-1.1.1.jar [exists ], <WORKSPACE>/common/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/js/compile.dest/classes [exists ], <WORKSPACE>/client/compile-resources [missing ], <WORKSPACE>/out/mill-bsp-out/common/js/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ], <WORKSPACE>/out/mill-bsp-out/client/compiledClassesAndSemanticDbFiles.dest/META-INF/best-effort [missing ]
Options:
-scalajs -Ywith-best-effort-tasty




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

java.lang.IndexOutOfBoundsException: 0
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
        (model.copy(lastCounter = model.counter, counter = n), Cmd.None)

      case Msg.Receive(ServerMsg.LoggedIn) =>
          (model.copy(isLoggedIn = true), Cmd.None)
      
      case Msg.Receive(ServerMsg.SetPos(r, c)) =>
          (model.copy(r = r,c = c,bt = model.bt.copy(
          active = Option(r,c)
        )),Cmd.None)


      case Msg.CoachView =>
        (model.copy(isCoachView = true), Cmd.None)

      case Msg.UpdatePasswordText(text) =>
        (model.copy(coachPassword = text), Cmd.None)
      case Msg.Tick(nowMs) =>
        val mAfterMain =
          model.chronoStart match
            case Some(start) => model.copy(chronoElapsedMs = nowMs - start)
            case None        => model

        val mAfterEmergency =
          mAfterMain.emergencyStart match
            case Some(estart) => mAfterMain.copy(emergencyElapsedMs = nowMs - estart)
            case None         => mAfterMain
        (mAfterEmergency, Cmd.None)

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
        val now = System.currentTimeMillis()
        def freezeIfRunning(m: Model): Model =
          m.chronoStart match
            case Some(start) =>
              // fige le chrono à l'instant présent
              m.copy(chronoStart = None, chronoElapsedMs = now - start)
            case None =>
              m // déjà à l'arrêt
        
        def resumeIfPaused(m: Model): Model =
          // repart du temps déjà accumulé (chronoElapsedMs)
          if (m.chronoStart.isEmpty && m.chronoElapsedMs > 0L)
            m.copy(chronoStart = Some(now - m.chronoElapsedMs))
          else
            m
        
        // --- helpers pour le chrono d'urgence ---
        def startEmergency(m: Model): Model =
          m.copy(emergencyStart = Some(now), emergencyElapsedMs = 0L)

        def stopEmergencyKeepResult(m: Model): Model =
          m.emergencyStart match
            case Some(s) => m.copy(emergencyStart = None, emergencyElapsedMs = now - s)
            case None    => m  // déjà arrêté

        def resetEmergency(m: Model): Model =
          m.copy(emergencyStart = None, emergencyElapsedMs = 0L)

        
        val model2 =
          payload match
            case "0" => // Démarrer : repart de 0 + on réinitialise le chrono d'urgence
              resetEmergency(model).copy(chronoStart = Some(now), chronoElapsedMs = 0L, obstacles = Set.empty)

            case "1" => // Arrêt d'urgence : fige le chrono principal + démarre le chrono d'urgence
              startEmergency(freezeIfRunning(model))

            case "2" => // Arrêt d'urgence : fige le chrono principal + démarre le chrono d'urgence
              startEmergency(freezeIfRunning(model))
            
            case "3" => // Arrêt d'urgence : fige le chrono principal + démarre le chrono d'urgence
              startEmergency(freezeIfRunning(model))
            
            case "4" => // Interruption : fige seulement le chrono principal
              freezeIfRunning(model)

            case "5" => // Reprise robot : stoppe l'urgence (garde le temps) + reprend le principal
              resumeIfPaused(stopEmergencyKeepResult(model))

            case "6" => // Autotest : ne touche à aucun chrono ici
              model

            case _ =>
              model

        model.bt.selectedId match
          case None =>
            (model2, Cmd.emit(Msg.Bluetooth(BluetoothMsg.Error("Aucun appareil sélectionné"))))
          case Some(id) =>
            val io: IO[Msg] =
              IO.fromFuture(IO(BluetoothJS.writeTextById(id, payload))).attempt.map {
                case Right(_)   => Msg.Bluetooth(BluetoothMsg.Wrote(payload))
                case Left(err)  => Msg.Bluetooth(BluetoothMsg.Error(Option(err.getMessage).getOrElse(err.toString)))
              }
            (model2, Cmd.Run(io))


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

        val autoTestStarted  = completeLines.exists(_.trim == "AUTOTEST: DEBUT")
        val autoTestFinished = completeLines.exists(_.trim == "AUTOTEST: FIN")
        val mechEmergency = completeLines.exists(_.trim.equalsIgnoreCase("ARRET URGENCE MECANIQUE"))
        val emergencyAutoCmd: Cmd[IO, Msg] =
          if mechEmergency && model.emergencyStart.isEmpty then
            Cmd.Emit(Msg.Bluetooth(BluetoothMsg.WriteToSelected("2")))
          else
            Cmd.None
        val mechEmergency1 = completeLines.exists(_.trim.equalsIgnoreCase("ARRET URGENCE ELECTRIQUE"))
        val emergencyAutoCmd1: Cmd[IO, Msg] =
          if mechEmergency1 && model.emergencyStart.isEmpty then
            Cmd.Emit(Msg.Bluetooth(BluetoothMsg.WriteToSelected("3")))
          else
            Cmd.None

        // 1) Si "DEBUT" reçu -> démarrer le chrono (réinitialisé)
        val modelAfterStart =
          if autoTestStarted then
            model.copy(chronoStart = Some(System.currentTimeMillis()), chronoElapsedMs = 0L)
          else
            model

        // 2) Si "FIN" reçu -> figer le chrono (chronoStart -> None, chronoElapsedMs := écoulé)
        val modelAfterAutoTest =
          if autoTestFinished && modelAfterStart.chronoStart.isDefined then
            val now     = System.currentTimeMillis()
            val elapsed = now - modelAfterStart.chronoStart.get
            modelAfterStart.copy(chronoStart = None, chronoElapsedMs = elapsed)
          else
            modelAfterStart


        
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

        
        val PosRE = raw"""\bPOS\s+(\d+)\s+(\d+)(?:\s+([A-Za-z]))?\b""".r
        val lastPosWithDirOpt: Option[(Int, Int, Option[String])] =
          completeLines.collect {
            case PosRE(r, c, d) =>
              val rr = math.max(1, math.min(5, r.toInt))
              val cc = math.max(1, math.min(4, c.toInt))
              val dirOpt = Option(d).map(_.toUpperCase)
              (rr, cc, dirOpt)
          }.lastOption

        //val lastPosOptDetecte: Option[(Int, Int)] = lastPosWithDirOpt.map { case (r, c, _) => (r, c) }
        val addedObstacle: Option[(Int, Int)] =
          lastPosWithDirOpt.collect { case (r, c, Some(_)) => (r, c) }
        val newObstacles: Set[(Int, Int)] =
           addedObstacle.map(rc => model.obstacles + rc).getOrElse(model.obstacles)
        
        //Point reçu du robot
     
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
  
        val changePosCmd: Cmd[IO, Msg] =
          lastPosOpt
            .map((r,c) => Cmd.Emit(Msg.Send(CoachMsg.Coor(r,c))))
            .getOrElse(Cmd.None)

        val Ballon = raw"""\bBallon\s+(\d+)\b""".r
        val lastBallonOpt: Option[Int] =
          completeLines.collect { case Ballon(n) => n.toInt }.lastOption
        val hasBall = lastBallonOpt match
          case Some(1) => true
          case Some(0) => false
          case _ => model.hasBall

        val m2 = modelAfterAutoTest.copy(
          hasBall = hasBall,
          obstacles = newObstacles,
          bt = model.bt.copy(
            notifications = newLog,
            buffer = newBuf,
            active = lastPosOpt.orElse(model.bt.active)
          )
        )

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
        val combinedCmd = Cmd.Batch(scrollCmd, incrCmd5,incrCmd2,changePosCmd,emergencyAutoCmd,emergencyAutoCmd1)

        (m2, combinedCmd)

      case Msg.Bluetooth(BluetoothMsg.Error(e)) =>
        (model.copy(bt = model.bt.copy(scanning = false, error = Some(e))), Cmd.None)

     
     /* case Msg.Bluetooth(_) =>
        (model, Cmd.None)*/

    }

  // ---------- Menu Bluetooth ----------
  private def bluetoothMenu(model: Model): Html[Msg] =
  details(cls := "collapse bg-base-100 border-base-300 border")(
    summary(cls := "collapse-title font-semibold")("Bluetooth (Makeblock)"),
    div(cls := "collapse-content")(
      p(
        if model.bt.scanning then "Recherche en cours…"
        else "Sélectionne un appareil Makeblock puis connecte-toi."
      ),
      // Boutons d'action
      div(
        button(
          cls := "btn btn-primary",
          onClick(Msg.Bluetooth(BluetoothMsg.StartScan))
        )("Rechercher des appareils Makeblock"),
        text(" "),
        button(
          cls := "btn btn-primary",
          onClick(Msg.Bluetooth(BluetoothMsg.ConnectSelected)),
          disabled(model.bt.selectedId.isEmpty)
        )("Se connecter") // <-- unique bouton "Se connecter"
      ),
      // Sélecteur
      div(
        label(attr("for") := "bt-device-select")("Appareils : "),
        select(
          id := "bt-device-select",
          cls := "select",
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
      // Liste d'infos + actions par appareil
      ul(
        model.bt.devices.map { d =>
          li(
            span(s"${d.name} [${d.id}] "),
            if d.connected then
              button(
                cls := "btn btn-primary",
                onClick(Msg.Bluetooth(BluetoothMsg.Disconnect(d.id)))
              )("Se déconnecter")
            else
              // On NE propose plus "Se connecter" ici, juste "Sélectionner"
              button(
                cls := "btn",
                onClick(Msg.Bluetooth(BluetoothMsg.SelectDevice(d.id)))
              )("Sélectionner")
          )
        }
      ),
      model.bt.error.fold[Html[Msg]](span())(err => p(cls := "text-error")(err))
    )
  )
  // --- Formatage du chrono en mm:ss ---
  private def fmtMmSs(ms: Long): String =
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    f"$minutes%02d:$seconds%02d"



  // ---------- Vue "terrain simple" : 4 colonnes x 5 lignes, une case active ----------
  private def fieldView(model: Model): Html[Msg] =
    val activeOpt: Option[(Int, Int)] = Option(model.r,model.c)  // (row, col) 1..5, 1..4

    val rows = 5
    val cols = 4

    val cells: List[Html[Msg]] =
      (1 to rows).toList.flatMap { r =>
        (1 to cols).toList.map { c =>
          val isActive   = activeOpt.contains((r, c))
          val isObstacle = model.obstacles.contains((r, c))

          val styleBase =
            "flex items-center justify-center border border-gray-300 rounded-lg text-sm select-none"

          val styleColor =
            if isObstacle && isActive then
              "bg-gradient-to-br from-yellow-300 to-green-500 text-gray-900 font-bold"
            else if isObstacle then
              "bg-yellow-300 text-gray-900 font-bold"
            else if isActive then
              "bg-green-500 text-white font-bold"
            else
              "bg-gray-100 text-gray-700"

          div(cls := s"$styleBase $styleColor")(s"R$r-C$c")
        }
      }


    div(cls := "flex justify-center mt-6")(
    div(cls := "bg-white border border-gray-200 rounded-xl p-4 shadow-sm w-full max-w-[760px]")(
      h2(cls := "mb-3 text-base font-semibold text-center")("Terrain (26 cm × 18 cm)"),
      div(
        cls := "grid grid-cols-4 [grid-template-rows:repeat(5,64px)] gap-2.5 w-full max-w-[700px] mx-auto aspect-[26/18]"
      )(
        cells*
      )
    )
  )
  // ---------- Vue principale connectée ----------
  def loggedInView(model: Model): Html[Msg] =
  div(
    // conteneur principal
    cls := "max-w-[980px] mx-auto mt-5 p-4 font-sans"
  )(

    // Header
    div(cls := "flex items-center justify-between gap-3 mb-4")(
      div(
        h1(cls := "text-[22px] m-0 tracking-[0.2px]")("Coach • Contrôle matériel"),
      ),
      span(
        cls :=
          (if model.bt.selectedId.isEmpty
           // Aucun appareil
           then "px-2.5 py-1.5 rounded-full text-[12px] bg-amber-50 text-amber-900 border border-amber-200"
           // Appareil sélectionné
           else "px-2.5 py-1.5 rounded-full text-[12px] bg-sky-50 text-sky-800 border border-sky-200")
      )(
        if model.bt.selectedId.isEmpty then "Aucun appareil sélectionné" else "Appareil sélectionné"
      )
    ),

    // Carte : Commandes rapides
    // --- Carte : Commandes rapides ---
    div(cls := "bg-white border border-gray-200 rounded-xl p-4 shadow-sm mb-3.5")(
      h2(cls := "mb-3 text-base font-semibold")("Commandes rapides"),

      {
        // Vérifie si un appareil est sélectionné ET connecté
        val connected = model.bt.devices.exists(_.connected)


        div(cls := "grid [grid-template-columns:repeat(auto-fit,minmax(140px,1fr))] gap-2.5")(
          button(onClick(Msg.Send(CoachMsg.Incr(2))),  cls := "btn btn-primary")("+2"),
          button(onClick(Msg.Send(CoachMsg.Incr(5))),  cls := "btn btn-primary")("+5"),
          button(onClick(Msg.Send(CoachMsg.Incr(3))),  cls := "btn btn-primary")("+3"),
          button(onClick(Msg.Send(CoachMsg.Reset)),    cls := "btn btn-warning")("Reset"),

          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("0"))),
            disabled(!connected), // désactivé si pas connecté
            cls := "btn btn-success"
          )("Démarrer"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("1"))),
            disabled(!connected),
            cls := "btn btn-error"
          )("Arrêt d'urgence"),


          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("2"))),
            disabled(!connected),
            cls := "btn btn-error"
          )("Arrêt d'urgence mécanique"),


          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("3"))),
            disabled(!connected),
            cls := "btn btn-error"
          )("Arrêt d'urgence électrique"),

          
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("4"))),
            disabled(!connected),
            title := "Lancer les tests matériels (commande '4')",
            cls := "btn btn-primary"
          )("Interruption"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("5"))),
            disabled(!connected),
            cls := "btn"
          )("Reprendre"),
          button(
            onClick(Msg.Bluetooth(BluetoothMsg.WriteToSelected("6"))),
            disabled(!connected),
            title := "Autotest MegaPi (commande '6')",
            cls := "btn btn-secondary"
          )("Autotest")
        )
      }
    ),
    div(cls := "mt-3 flex gap-2 justify-end")(
      {
        val (label, badgeCls) =
          if model.chronoStart.isDefined then
            (s"Chrono : ${fmtMmSs(model.chronoElapsedMs)}",
            "bg-emerald-50 text-emerald-700 border border-emerald-200")
          else if model.chronoElapsedMs > 0 then
            (s"Chrono : ${fmtMmSs(model.chronoElapsedMs)} (arrêt)",
            "bg-amber-50 text-amber-800 border border-amber-200")
          else
            ("Chrono : --:--",
            "bg-slate-100 text-slate-700 border border-slate-200")
        span(cls := s"text-[12px] px-2.5 py-1.5 rounded-full $badgeCls")(text(label))
      },
      {
        val (elabel, ebadge) =
          if model.emergencyStart.isDefined then
            (s"Urgence : ${fmtMmSs(model.emergencyElapsedMs)}",
            "bg-rose-50 text-rose-700 border border-rose-200")
          else if model.emergencyElapsedMs > 0 then
            (s"Urgence : ${fmtMmSs(model.emergencyElapsedMs)} (figé)",
            "bg-rose-100 text-rose-800 border border-rose-300")
          else
            ("Urgence : --:--",
            "bg-slate-100 text-slate-700 border border-slate-200")
        span(cls := s"text-[12px] px-2.5 py-1.5 rounded-full $ebadge")(text(elabel))
      }
    ),


    // Carte : Bluetooth
    div(cls := "bg-white border border-gray-200 rounded-xl p-4 shadow-sm mb-3.5")(
      h2(cls := "mb-3 text-base font-semibold")("Bluetooth"),
      bluetoothMenu(model)
    ),

    // Carte : Log
    div(cls := "bg-white border border-gray-200 rounded-xl p-4 shadow-sm mb-3.5")(
      div(cls := "flex items-center justify-between mb-2.5")(
        h2(cls := "m-0 text-base font-semibold")("Retours du module"),
        span(cls := "text-[12px] text-gray-500")(s"${model.bt.notifications.length} ligne(s)")
      ),
      div(
        id := "bt-log",
        cls := "h-[420px] max-h-[55vh] overflow-y-auto border border-dashed border-gray-300 rounded-[10px] p-2.5 font-mono whitespace-pre-wrap leading-[1.28] bg-gray-50"
      )(
        pre(cls := "m-0")(model.bt.notifications.mkString("\n"))
      ),
      model.bt.error.fold[Html[Msg]](div())(err =>
        div(cls := "mt-2.5 text-[13px] px-2.5 py-2 rounded-lg bg-rose-50 text-rose-800 border border-rose-200")(
          s"Erreur Bluetooth : $err"
        )
      )
    ),

    // Carte : État du ballon
    div(cls := "bg-white border border-gray-200 rounded-xl p-4 shadow-sm mb-3.5")(
  h2(cls := "mb-3 text-base font-semibold")("État du ballon"),

  div(
    cls := s"flex items-center justify-center gap-2 h-20 rounded-xl text-lg font-semibold " +
      (if model.hasBall then "text-green-600" else "text-red-600")
  )(
    // Le petit rond coloré
    span(
      cls := s"inline-block w-3 h-3 rounded-full " +
        (if model.hasBall then "bg-green-600" else "bg-red-600")
    )(),
    // Le texte
    text(if model.hasBall then "Ballon détecté" else "Pas de ballon")
  )
)
)
  


  // ---------- Vue mot de passe ----------
  def passwordView(model: Model): Html[Msg] =
    
    div(cls := "max-w-md mx-auto")(
    
      div(cls:="bg-white border border-slate-100 rounded-xl p-7 shadow-md")(
        h2(cls := "mb-2 text-md")("Accès coach"),
        p(cls := "mb-3.5 text-gray-500 text-[13px]")(
          text("Saisissez le mot de passe pour débloquer les commandes avancées.")
        ),
        div(
          label(cls:="mb-1 text-sm")("Mot de passe"),
          input(
            cls := "input w-full",
            tpe := "password",
            onInput(Msg.UpdatePasswordText.apply)
          )
        ),
        div(cls := "h-2.5")(),
        button(onClick(Msg.Send(CoachMsg.Login(model.coachPassword))), cls := "btn btn-primary w-full")("Se connecter")
      )
    )

  // ---------- View ----------
  def view(model: Model): Html[Msg] =
  div(
    // fond global
    cls := "min-h-screen bg-[#f5f7fb]"
  )(
    // --- Si connecté ---
    if model.isLoggedIn then
      div()(
        // Compteur à droite
        div(cls := "max-w-[980px] mx-auto mt-4 px-4 flex justify-end")(
          span(
            cls := "text-[12px] px-2.5 py-1.5 rounded-full bg-indigo-50 text-indigo-800 border border-indigo-200"
          )(
            s"Compteur : ${model.counter}"
          )
        ),
        // Contenu connecté
        loggedInView(model),
        div(cls := "h-4")(),
        fieldView(model)
      )

    // --- Si on demande un mot de passe ---
    else if model.shouldAskPassword then
      passwordView(model)

    // --- Sinon (pas connecté) ---
    else {
      val diff = model.counter - model.lastCounter
      val messageOpt =
        if diff == 5 then Some("Essai")
        else if diff == 2 then Some("Transformation")
        else if diff == 3 then Some("Pénalité")
        else None

      div(cls := "max-w-[980px] mx-auto px-4 mt-16")(
        // bloc centré
        div(cls := "bg-white border border-gray-200 rounded-2xl p-10 shadow-sm text-center mx-auto max-w-xl space-y-6")(

          // compteur centré
          div(cls := "flex justify-center")(
            span(
              cls := "text-[13px] px-3 py-1.5 rounded-full bg-indigo-50 text-indigo-800 border border-indigo-200"
            )(
              s"Compteur : ${model.counter}"
            )
          ),
        
          // message selon le diff
          messageOpt.fold[Html[Msg]](div())(msg =>
            div(cls := "inline-flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-50 text-emerald-700 text-sm font-medium mx-auto")(
              // petit rond
              span(cls := "w-2 h-2 rounded-full bg-emerald-500 inline-block")(),
              text(msg)
            )
          )
        )
      )
    }

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
    import scala.concurrent.duration.*  // <- pour 1.second

    val chronoSub: Sub[IO, Msg] = 
      if model.chronoStart.isDefined || model.emergencyStart.isDefined then
        Sub.every[IO](1.second).map(d => Msg.Tick(d.getTime().toLong))
      else
        Sub.None


    

    Sub.Batch(wsSub, btNotifySub,chronoSub)

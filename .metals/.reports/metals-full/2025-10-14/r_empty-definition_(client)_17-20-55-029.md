error id: file://<WORKSPACE>/client/src/fr/cyu/robafis/Main.scala:
file://<WORKSPACE>/client/src/fr/cyu/robafis/Main.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -tyrian/Html.m2.
	 -tyrian/Html.m2#
	 -tyrian/Html.m2().
	 -tyrian/m2.
	 -tyrian/m2#
	 -tyrian/m2().
	 -scala/scalajs/js/annotation/m2.
	 -scala/scalajs/js/annotation/m2#
	 -scala/scalajs/js/annotation/m2().
	 -m2.
	 -m2#
	 -m2().
	 -scala/Predef.m2.
	 -scala/Predef.m2#
	 -scala/Predef.m2().
offset: 1594
uri: file://<WORKSPACE>/client/src/fr/cyu/robafis/Main.scala
text:
```scala
package fr.cyu.robafis

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*
import tyrian.websocket.WebSocket
import tyrian.websocket.WebSocketConnect
import tyrian.websocket.WebSocketEvent

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg =
    case loc: Location.Internal =>
      if loc.pathName == "/coach" then Msg.CoachView
      else Msg.NoOp
    
    case _: Location.External => Msg.NoOp

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.empty, Cmd.emit(Msg.Connect))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp => (model, Cmd.None)
    case Msg.Connect => (
      model,
      WebSocket.connect[IO, Msg]("/connect"):
        case WebSocketConnect.Error(error) =>
          println(s"ERROR: $error")
          Msg.NoOp
        case WebSocketConnect.Socket(socket) => Msg.Connected(socket)
    )

    case Msg.Connected(socket) =>
      println("Connected to socket")
      (model.copy(socket = Some(socket)), Cmd.None)

    case Msg.Send(msg) =>
      (model, model.socket.fold(Cmd.None)(_.publish(upickle.write(msg))))

    case Msg.Receive(msg) => msg match
      case ServerMsg.SetCount(n) => (model.copy(counter = n), Cmd.None)
      case ServerMsg.LoggedIn => (model.copy(isLoggedIn = true), Cmd.None)

    case Msg.CoachView => (model.copy(isCoachView = true), Cmd.None)
    case Msg.UpdatePasswordText(text) => (model.copy(coachPassword = text), Cmd.None)

      // --- Bluetooth msgs ---
    case Msg.
      val m2@@ = model.copy(bt = model.bt.copy(scanning = true, error = None))
      // TODO: déclencher le picker / scan (navigator.bluetooth.requestDevice) et renvoyer DeviceListUpdated / Error
      (m2, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.DeviceListUpdated(devs)) =>
      val m2 = model.copy(bt = model.bt.copy(scanning = false, devices = devs, error = None))
      (m2, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.SelectDevice(id)) =>
      val m2 = model.copy(bt = model.bt.copy(selectedId = Some(id), error = None))
      (m2, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.ConnectSelected) =>
      // TODO: lancer la connexion GATT pour selectedId puis renvoyer Connected(id) / Error
      (model, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.Connected(id)) =>
      val m2 = model.copy(bt =
        model.bt.copy(
          devices = model.bt.devices.map(d => if d.id == id then d.copy(connected = true) else d),
          error   = None
        )
      )
      (m2, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.Disconnect(id)) =>
      // TODO: déconnecter GATT puis renvoyer Disconnected(id)
      (model, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.Disconnected(id)) =>
      val m2 = model.copy(bt =
        model.bt.copy(
          devices = model.bt.devices.map(d => if d.id == id then d.copy(connected = false) else d),
          error   = None
        )
      )
      (m2, Cmd.None)

    case Msg.Bluetooth(BluetoothMsg.Error(e)) =>
      val m2 = model.copy(bt = model.bt.copy(scanning = false, error = Some(e)))
      (m2, Cmd.None)




  val loggedInView: Html[Msg] = div(
    button(onClick(Msg.Send(CoachMsg.Incr(2))))("+2"),
    button(onClick(Msg.Send(CoachMsg.Incr(5))))("+5"),
    button(onClick(Msg.Send(CoachMsg.Reset)))("Reset")
  )

  def passwordView(model: Model): Html[Msg] = div(
    label("Password:"),
    input(tpe := "password", onInput(Msg.UpdatePasswordText.apply)),
    button(onClick(Msg.Send(CoachMsg.Login(model.coachPassword))))("Login")
  )

  def view(model: Model): Html[Msg] = div(
    label(model.counter.toString),
    br(),
    if model.isLoggedIn then loggedInView
    else div(),
    if model.shouldAskPassword then passwordView(model)
    else div()
  )

  def subscriptions(model: Model): Sub[IO, Msg] =
    model.socket.fold(Sub.None)(_.subscribe:
      case WebSocketEvent.Heartbeat => Msg.NoOp
      case WebSocketEvent.Open => Msg.NoOp
      case WebSocketEvent.Close(code, reason) => Msg.NoOp
      case WebSocketEvent.Error(error) =>
        println(s"ERROR event: $error")
        Msg.NoOp
      case WebSocketEvent.Receive(message) =>
        println(s"Received: $message")
        Msg.Receive(upickle.read(message))
    )
```


#### Short summary: 

empty definition using pc, found symbol in pc: 
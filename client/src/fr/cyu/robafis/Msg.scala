package fr.cyu.robafis

import cats.effect.IO
import tyrian.websocket.WebSocket

enum BluetoothMsg:
  case StartScan
  case DeviceListUpdated(devs: List[BtDevice])
  case SelectDevice(id: String)
  case ConnectSelected
  case Connected(id: String)
  case Disconnect(id: String)
  case Disconnected(id: String)
  case Error(msg: String)
  case WriteToSelected(payload: String)
  case Wrote(payload: String)
  case EnableNotifySelected
  case NotificationReceived(payload: String)

enum Msg:
  case NoOp
  case Connect
  case Connected(socket: WebSocket[IO])
  case Send(msg: CoachMsg)
  case Receive(msg: ServerMsg)
  case CoachView
  case UpdatePasswordText(text: String)
  case Bluetooth(m: BluetoothMsg)

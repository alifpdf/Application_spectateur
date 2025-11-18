package fr.cyu.robafis

import org.scalajs.dom
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.UndefOrOps
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.typedarray.{Uint8Array, DataView}
import scala.collection.mutable

// --- Façades globales WHATWG Encoding (compat partout) ---
@js.native
@JSGlobal("TextEncoder")
class TextEncoder() extends js.Object:
  def encode(input: String): Uint8Array = js.native

@js.native
@JSGlobal("TextDecoder")
class TextDecoder(label: String = "utf-8") extends js.Object:
  /** decode(input?, options?) — options.stream = true pour le streaming */
  def decode(input: js.UndefOr[js.Any] = js.undefined,
             options: js.UndefOr[js.Dictionary[Boolean]] = js.undefined): String = js.native

object BluetoothJS:

  // --- Types JS minimalistes ---
  trait BluetoothDevice extends js.Object:
    val id: String
    val name: js.UndefOr[String]
    val gatt: js.UndefOr[BluetoothRemoteGATTServer]

  trait BluetoothRemoteGATTServer extends js.Object:
    def connect(): js.Promise[BluetoothRemoteGATTServer]
    def disconnect(): Unit
    val connected: Boolean
    def getPrimaryService(uuid: String): js.Promise[BluetoothRemoteGATTService]

  trait BluetoothRemoteGATTService extends js.Object:
    def getCharacteristic(uuid: String): js.Promise[BluetoothRemoteGATTCharacteristic]

  trait BluetoothRemoteGATTCharacteristic extends js.Object:
    def writeValue(data: Uint8Array): js.Promise[Unit]
    def startNotifications(): js.Promise[BluetoothRemoteGATTCharacteristic]
    val value: js.UndefOr[DataView]
    def addEventListener(`type`: String, listener: js.Function1[dom.Event, Any]): Unit

  // --- UUIDs UART Makeblock ---
  private val UART_SERVICE     = "0000ffe1-0000-1000-8000-00805f9b34fb"
  private val UART_CHAR_WRITE  = "0000ffe3-0000-1000-8000-00805f9b34fb" // Write
  private val UART_CHAR_NOTIFY = "0000ffe2-0000-1000-8000-00805f9b34fb" // Notify

  // --- Registre local des devices JS ---
  private val registry: mutable.Map[String, BluetoothDevice] = mutable.Map.empty

  private def remember(dev: BluetoothDevice): BluetoothDevice =
    registry.update(dev.id, dev); dev

  private def get(id: String): Option[BluetoothDevice] =
    registry.get(id)

    

  /** Ouvre le picker filtré Makeblock (namePrefix). */
  def requestMakeblockDevice(
      optionalServices: Seq[String] = Seq(UART_SERVICE)
  ): Future[BluetoothDevice] =
    val nav = dom.window.navigator.asInstanceOf[js.Dynamic]
    val bt  = nav.bluetooth
    if js.isUndefined(bt) || bt == null then
      Future.failed(new Exception(
        "Web Bluetooth indisponible. Utilise Chrome/Edge et sers l’app en HTTPS ou sur http://localhost."
      ))
    else
      val filters = js.Array(
        js.Dynamic.literal(namePrefix = "Makeblock"),
        js.Dynamic.literal(namePrefix = "makeblock")
      )
      val opts = js.Dynamic.literal(
        filters = filters,
        optionalServices = optionalServices.toJSArray
      )
      val p = bt.requestDevice(opts).asInstanceOf[js.Promise[BluetoothDevice]]
      p.toFuture.map(remember)

  private def ensureConnected(gatt: BluetoothRemoteGATTServer): Future[BluetoothRemoteGATTServer] =
    if gatt.connected then Future.successful(gatt) else gatt.connect().toFuture

  
  private def withGatt[A](id: String)(
  f: (BluetoothDevice, BluetoothRemoteGATTServer) => Future[A]): Future[A] =
    get(id) match
      case None =>
        Future.failed(new Exception(s"Appareil inconnu: $id"))
      case Some(dev) =>
        dev.gatt.toOption match
          case None =>
            Future.failed(new Exception("GATT non disponible sur cet appareil"))
          case Some(gatt) =>
            f(dev, gatt)

  
  
  def connectById(id: String): Future[String] =
    withGatt(id) { (_, gatt) =>
      gatt.connect().toFuture.map(_ => id)
    }


  def disconnectById(id: String): Future[String] =
    get(id) match
      case Some(dev) =>
        dev.gatt.toOption.foreach(_.disconnect())
        Future.successful(id)
      case None =>
        Future.failed(new Exception(s"Appareil inconnu: $id"))

  // --- UTF-8 helpers ---
  private val textEncoder = new TextEncoder()
  private val utf8Decoder = new TextDecoder("utf-8") // streaming reuse
  private def toUint8(text: String): Uint8Array =
    textEncoder.encode(text)

  /** Écrit du texte UTF-8 sur la characteristic UART RX (Write). */
  def writeTextById(id: String, text: String): Future[Unit] =
    withGatt(id) { (_, gatt) =>
      for
        g   <- ensureConnected(gatt)
        srv <- g.getPrimaryService(UART_SERVICE).toFuture
        ch  <- srv.getCharacteristic(UART_CHAR_WRITE).toFuture
        _   <- ch.writeValue(toUint8(text)).toFuture
      yield ()
    }

  /** Active les notifications (FFE2) et émet CustomEvent("bt-notify", { detail: "<texte UTF-8>" }). */
  def enableNotifyById(id: String): Future[Unit] =
    withGatt(id) { (_, gatt) =>
      for
        g   <- ensureConnected(gatt)
        srv <- g.getPrimaryService(UART_SERVICE).toFuture
        ch  <- srv.getCharacteristic(UART_CHAR_NOTIFY).toFuture
        _   <- ch.startNotifications().toFuture
        _    = ch.addEventListener(
                "characteristicvaluechanged",
                (_: dom.Event) => {
                  ch.value.toOption.foreach { dv =>
                    val u8 = new Uint8Array(dv.buffer, dv.byteOffset, dv.byteLength)
                    val text = utf8Decoder.decode(
                      u8,
                      js.Dictionary("stream" -> true)
                    )

                    val init = (new js.Object).asInstanceOf[dom.CustomEventInit]
                    init.detail = text.asInstanceOf[js.Any]
                    val evt = new dom.CustomEvent("bt-notify", init)
                    dom.window.dispatchEvent(evt)
                  }
                }
              )
      yield ()
    }


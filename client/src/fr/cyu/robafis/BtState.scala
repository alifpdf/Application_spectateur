package fr.cyu.robafis

final case class BtDevice(id: String, name: String, connected: Boolean)

final case class BtState(
  scanning: Boolean = false,
  error: Option[String] = None,
  devices: List[BtDevice] = Nil,
  selectedId: Option[String] = None,
  notifications: List[String] = Nil,
   buffer: String = "" ,
   active: Option[(Int, Int)] = None 
)

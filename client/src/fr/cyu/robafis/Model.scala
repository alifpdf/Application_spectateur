package fr.cyu.robafis

import cats.effect.IO
import tyrian.websocket.WebSocket

case class Model(
  counter: Int,
  isCoachView: Boolean,
  isLoggedIn: Boolean,
  coachPassword: String,
  socket: Option[WebSocket[IO]],
  bt: BtState,                    // <-- on référence seulement BtState ici
  r : Int,
  c : Int
):
  def shouldAskPassword: Boolean = isCoachView && !isLoggedIn

object Model:
  val empty: Model = Model(
    counter = 0,
    isCoachView = false,
    isLoggedIn = false,
    coachPassword = "",
    socket = None,
    bt = BtState(),               // <-- constructeur par défaut depuis BtState.scala
    r = 3,
    c = 3
  )

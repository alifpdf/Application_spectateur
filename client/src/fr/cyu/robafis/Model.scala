package fr.cyu.robafis

import cats.effect.IO
import tyrian.websocket.WebSocket

case class Model(
  counter: Int,
  lastCounter: Int,
  isCoachView: Boolean,
  isLoggedIn: Boolean,
  coachPassword: String,
  socket: Option[WebSocket[IO]],
  bt: BtState,                    // <-- on référence seulement BtState ici
  r : Int,
  c : Int,
  hasBall: Boolean = false,
  chronoStart: Option[Long],   // None si arrêté, Some(epochMs) si en cours
  chronoElapsedMs: Long,      // dernier calcul pour l'affichage du chrono
  emergencyStart: Option[Long],
  emergencyElapsedMs: Long,
  obstacles: Set[(Int, Int)]
  
):
  def shouldAskPassword: Boolean = isCoachView && !isLoggedIn

object Model:
  val empty: Model = Model(
    counter = 0,
    lastCounter = 0,
    isCoachView = false,
    isLoggedIn = false,
    coachPassword = "",
    socket = None,
    bt = BtState(),               // <-- constructeur par défaut depuis BtState.scala
    r = 3,
    c = 3,
    hasBall = false,
    chronoStart = None,
    chronoElapsedMs = 0L,
    emergencyStart = None,
    emergencyElapsedMs = 0L,
    obstacles = Set.empty
    
  )

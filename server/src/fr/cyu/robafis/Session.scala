package fr.cyu.robafis

case class Session(connection: cask.WsChannelActor, isCoach: Boolean)
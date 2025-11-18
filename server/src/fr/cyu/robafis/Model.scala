package fr.cyu.robafis

case class Model(counter: Int, sessions: Map[Int, Session], nextSessionId: Int,r: Int, c: Int):

  def withNewSession(channel: cask.WsChannelActor): (Int, Model) =
    (
      this.nextSessionId,
      this.copy(sessions = sessions.updated(nextSessionId, Session(channel, false)), nextSessionId = nextSessionId + 1)
    )

  def promote(id: Int): Model =
    sessions.get(id) match
      case Some(session) => this.copy(sessions = sessions.updated(id, session.copy(isCoach = true)))
      case None => this
    
  def isCoach(id: Int): Boolean =
    sessions.get(id).exists(_.isCoach)

  def sendToAll(msg: ServerMsg): Unit =
    sessions.values.foreach(session => session.connection.send(cask.Ws.Text(upickle.write(msg))))

  def removeSession(id: Int): Model = 
    this.copy(sessions=sessions.removed(id))

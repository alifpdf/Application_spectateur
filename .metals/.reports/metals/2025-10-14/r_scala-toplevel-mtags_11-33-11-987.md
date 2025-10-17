error id: file://<WORKSPACE>/server/src/fr/cyu/robafis/Model.scala:[601..601) in Input.VirtualFile("file://<WORKSPACE>/server/src/fr/cyu/robafis/Model.scala", "package fr.cyu.robafis

case class Model(counter: Int, sessions: Map[Int, Session], nextSessionId: Int):

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

  def ")
file://<WORKSPACE>/file:<WORKSPACE>/server/src/fr/cyu/robafis/Model.scala
file://<WORKSPACE>/server/src/fr/cyu/robafis/Model.scala:19: error: expected identifier; obtained eof
  def 
      ^
#### Short summary: 

expected identifier; obtained eof
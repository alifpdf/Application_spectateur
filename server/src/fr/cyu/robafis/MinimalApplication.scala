package fr.cyu.robafis

import upickle.*
import cask.model.StaticResource
import cask.model.Response
import scala.concurrent.blocking
import scala.concurrent.Future

object MinimalApplication extends cask.MainRoutes:

  var model = Model(0, Map.empty, 0,3,3)

  val password = "abc"

  override def host: String = "0.0.0.0"
  override def port: Int = 8080

  @cask.staticResources("/static")
  def staticResourceRoutes() = "."

  @cask.get("/")
  def indexSpec() = Response(
    data = os.read(os.resource / "index.html"),
    headers = Seq("Content-Type" -> "text/html")
  )

  @cask.get("/coach")
  def indexCoach() = indexSpec()

  @cask.websocket("/connect")
  def specWS(): cask.WebsocketResult =
    cask.WsHandler( channel =>
      val res = model.withNewSession(channel)
      val id = res._1
      model = res._2

      channel.send(cask.Ws.Text(upickle.write(ServerMsg.SetCount(model.counter))))

      cask.WsActor:
        case cask.Ws.ChannelClosed() =>
          model=model.removeSession(id)
        case cask.Ws.Close(_, _) => 
          model = model.removeSession(id)
        case cask.Ws.Text(json) =>
          val coachMsg = upickle.read[CoachMsg](json)
          coachMsg match
            case CoachMsg.Login(password) =>
              if password == this.password then
                println(s"Authentication success for session $id")
                model = model.promote(id)
                channel.send(cask.Ws.Text(upickle.write(ServerMsg.LoggedIn)))
              else
                println(s"Authentication failed for session $id")
            
            case CoachMsg.Incr(n) =>
              println(s"Increment $n (from session $id)")
              if model.isCoach(id) then
                model = model.copy(counter = model.counter + n)
                model.sendToAll(ServerMsg.SetCount(model.counter))
            case CoachMsg.Reset =>
              if model.isCoach(id) then
                model = model.copy(counter=0)
                model.sendToAll(ServerMsg.SetCount(model.counter))
            case CoachMsg.Coor(r, c) => 
              if model.isCoach(id) then
                model =model.copy(r = r,c=c)
                model.sendToAll(ServerMsg.SetPos(model.r,model.c))
    )

  initialize()
package fr.cyu.robafis

import upickle.*

enum ServerMsg derives ReadWriter:
  case SetCount(n: Int)
  case LoggedIn
  case SetPos(r: Int, c: Int)
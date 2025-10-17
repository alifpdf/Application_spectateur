package fr.cyu.robafis

import upickle.*

enum CoachMsg derives ReadWriter:
  case Login(password: String)
  case Incr(n: Int)
  case Reset
  case Coor(r: Int,c: Int)
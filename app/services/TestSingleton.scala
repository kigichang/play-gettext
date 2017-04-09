package services

import javax.inject.Singleton

trait Test1 {
  def test1: String
}

trait Test2 {
  def test2: String
}

@Singleton
class MyTest extends Test1 with Test2 {
  private var counter = 0
  println(s"MyTest $counter")

  counter += 1

  override def test1: String = "test1"

  override def test2: String = "test2"
}
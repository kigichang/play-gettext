package services

import java.io
import java.io.File
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Environment}
import play.api.i18n._
import play.api.mvc.{RequestHeader, Result}
import play.mvc.Http
import scaposer.{I18n, Translation}

import scala.collection.mutable
/**
  * Created by kigi on 07/04/2017.
  */
trait MyPlayI18n extends MessagesApi {
  def loadPO(file: String): Option[I18n]
  def msg(singular: String, args: Any*)(implicit lang: Lang): String = t(singular, args:_ *)(lang)
  def t(singular: String, args: Any*)(implicit lang: Lang): String
  def tn(singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String
  def tc(context: String, singular: String, args: Any*)(implicit lang: Lang): String
  def tcn(context: String, singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String
}
@Singleton
class MyI18n @Inject() (environment: Environment, configuration: Configuration, langs: Langs) extends DefaultMessagesApi(environment, configuration, langs) with MyPlayI18n {

  private val poPath = configuration.getString("gettext.path").getOrElse(".")

  private val po: Map[String, I18n] = loadAllPO

  def loadPO(file: String): Option[I18n] = {
    println(s"load $poPath/$file")
    if (new File(s"$poPath/$file").exists()) {
      println(s"load $poPath/$file found")
      val source = scala.io.Source.fromFile(s"$poPath/$file")
      val content = try source.mkString finally source.close()

      scaposer.Parser.parse(content).fold(
        _ => None,
        result => Option(I18n(result))
      )
      //I18n(scaposer.Parser.parse(content).right.get)
    } else {
      println(s"load $poPath/$file not found")
      None
    }
  }

  private def loadAllPO: Map[String, I18n] = {
    (langs.availables.map(_.code) :+ "default").map(lang =>
      (lang, loadPO(s"$lang.po"))
    ).filter(_._2.isDefined).map(x => (x._1, x._2.get)).toMap
  }
  private def findPO(lang: Lang): Option[I18n] = {
    val codesToTry = Seq(lang.code, lang.language, "default")
    codesToTry.collectFirst { case lang if po.contains(lang) => po(lang) }
  }
  override def t(singular: String, args: Any*)(implicit lang: Lang): String = {
    findPO(lang) map { po =>
      println(s"po: ${po.t(singular)}")
      java.text.MessageFormat.format(po.t(singular), args.map(_.asInstanceOf[Object]): _*)
    } getOrElse(java.text.MessageFormat.format(singular, args.map(_.asInstanceOf[Object]): _*))
  }

  override def tn(singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String =
    findPO(lang) map { _.tn(singular, plural, n).formatLocal(lang.locale, args) } getOrElse(plural.formatLocal(lang.locale, n, args))
  override def tc(context: String, singular: String, args: Any*)(implicit lang: Lang): String =
    findPO(lang) map { _.tc(context, singular).formatLocal(lang.locale, args)} getOrElse(singular.formatLocal(lang.locale, args))
  override def tcn(context: String, singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String =
    findPO(lang) map { _.tcn(context, singular, plural, n).formatLocal(lang.locale, args)} getOrElse(plural.formatLocal(lang.locale, n, args))
}

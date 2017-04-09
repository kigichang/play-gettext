package services

import java.text.MessageFormat
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Environment}
import play.api.i18n.{DefaultMessagesApi, Lang, Langs}
import scaposer.I18n

import scala.util.Try

/**
  * Created by kigi on 4/8/17.
  */
trait PlayGetText {
  def t(singular: String, args: Any*)(implicit lang: Lang): String
  def tn(singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String
  def tc(context: String, singular: String, args: Any*)(implicit lang: Lang): String
  def tcn(context: String, singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String
  def msg(key: String, args: Any*)(implicit lang: Lang): String
}

@Singleton
class DefaultPlayGetText @Inject() (environment: Environment, configuration: Configuration, langs: Langs) extends PlayGetText {

  private val poPath = configuration.getString("gettext.path").getOrElse(".")

  private val po: Map[String, I18n] = loadAllPO

  private def findPO(lang: Lang): Option[I18n] = {
    val codesToTry = Seq(lang.code, lang.language, "default")
    codesToTry.collectFirst { case lang if po.contains(lang) => po(lang) }
  }

  def loadPO(file: String): Option[I18n] = {
    Try {
      val source = scala.io.Source.fromFile(s"$poPath/$file")
      val content = source.mkString
      source.close()
      I18n(scaposer.Parser.parse(content).right.get)
    }.toOption
  }

  private def loadAllPO: Map[String, I18n] = {
    langs.availables.map(_.code).map(lang =>
      (lang, loadPO(s"$lang.po"))
    ).filter(_._2.isDefined).map(x => (x._1, x._2.get)).toMap
  }

  private def translate(pattern: String, args: Seq[Any])(implicit lang: Lang): String =
    new MessageFormat(pattern, lang.toLocale).format(args.map(_.asInstanceOf[java.lang.Object]).toArray)

  private def enPlural(singular: String, plural: String, n: Long): String =
    if (n != 1) plural else singular

  override def msg(key: String, args: Any*)(implicit lang: Lang): String = findPO(lang) map { po => translate(po.t(key), args) } getOrElse { translate(key, args) }

  override def t(singular: String, args: Any*)(implicit lang: Lang): String = msg(singular, args:_ *)(lang)


  override def tn(singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String = {
    findPO(lang) map { po => translate(po.tn(singular, plural, n), args) } getOrElse { translate(enPlural(singular, plural, n), n +: args) }
  }

  override def tc(ctx: String, singular: String, args: Any*)(implicit lang: Lang): String = {
    findPO(lang) map { po => translate(po.tc(ctx, singular), args) } getOrElse { translate(singular, args) }
  }

  override def tcn(ctx: String, singular: String, plural: String, n: Long, args: Any*)(implicit lang: Lang): String = {
    findPO(lang) map { po => translate(po.tcn(ctx, singular, plural, n), args) } getOrElse { translate(enPlural(singular, plural, n), n +: args) }
  }


}

@Singleton
class MyMessagesApi @Inject()(i18n: PlayGetText, environment: Environment, configuration: Configuration, langs: Langs) extends DefaultMessagesApi(environment, configuration, langs) {

  override def translate(key: String, args: Seq[Any])(implicit lang: Lang): Option[String] = {
    val codesToTry = Seq(lang.code, lang.language, "default", "default.play")
    val pattern: Option[String] =
      codesToTry.foldLeft[Option[String]](None)((res, lang) =>
        res.orElse(messages.get(lang).flatMap(_.get(key))))
    println(s"get pattern $pattern")
    pattern map { i18n.msg(_, args:_ *) }
  }
}
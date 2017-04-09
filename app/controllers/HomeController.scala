package controllers

import javax.inject._

import play.api._
import play.api.i18n.{I18nSupport, Lang, Langs, MessagesApi}
import play.api.mvc._
import services.{MyI18n, MyPlayI18n, PlayGetText}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val messagesApi: MessagesApi, implicit val myI18n: PlayGetText, lang: Langs) extends Controller with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {

    lang.availables.foreach{ println(_) }
    //val po = myI18n.loadPO("default")
    //if (po.isDefined) {
    //  println(po.get.t("hello world"))
    //}
    println(myI18n.t("{0} tells {1}", "apple", "ms"))
    println(myI18n.tn("I have a {1}", "I have {0} {2}", 1, "dog", "dogs"))
    println(messagesApi("constraint.max", 100))
    println(messagesApi("error.real.precision", 5, 3))
    Ok(views.html.index("Your new application is ready."))

  }

}

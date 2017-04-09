package modules

import play.api.i18n.{DefaultLangs, Langs, MessagesApi}
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

/**
  * Created by kigi on 4/9/17.
  */
class I18nModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[Langs].to[DefaultLangs],
      bind[MessagesApi].to[services.MyMessagesApi],
      bind[play.i18n.MessagesApi].toSelf,
      bind[play.i18n.Langs].toSelf
    )
  }
}

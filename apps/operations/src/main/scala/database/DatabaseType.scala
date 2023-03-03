package es.eriktorr.lambda4s
package database

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class DatabaseType[A](columns: List[(String, String)])

object DatabaseType:
  import scala.quoted.{Expr, Quotes, Type}

  inline given apply[A]: DatabaseType[A] = DatabaseType[A](DatabaseType.databaseTypesOf[A])

  private inline def databaseTypesOf[A]: List[(String, String)] = ${ databaseTypesOfImpl[A] }

  private def databaseTypesOfImpl[A: Type](using
      quotes: Quotes,
  ): Expr[List[(String, String)]] =
    import quotes.reflect.*

    val tpe = TypeRepr.of[A]
    val symbol = tpe.typeSymbol

    if !(symbol.isClassDef && symbol.flags.is(Flags.Case)) then
      report.errorAndAbort(s"Expecting a case class, but got: ${summon[Type[A]]}")

    Expr(
      symbol.caseFields
        .map(field =>
          tpe.memberType(field).asType match
            case '[Double] => field.name -> "DoubleType"
            case '[Int] => field.name -> "IntType"
            case '[LocalDate] => field.name -> "DateType"
            case '[String] => field.name -> "StringType"
            case '[unknown] =>
              report.errorAndAbort(s"Unsupported type as database column: ${Type.show[unknown]}"),
        ),
    )

  lazy val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").nn
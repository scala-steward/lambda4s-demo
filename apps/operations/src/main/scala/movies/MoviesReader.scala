package es.eriktorr.lambda4s
package movies

import database.IoQuery.list
import database.{RowMapper, Transactor}
import movies.MoviesReader.{CumulativeRevenue, RatingCounter}

import cats.effect.IO
import org.typelevel.log4cats.Logger

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import scala.scalajs.js

trait MoviesReader:
  /** Count the number of films grouped by their rating.
    * @return
    *   the number of films in each rating
    */
  def filmsByRating: IO[List[RatingCounter]]

  /** Calculate the cumulative revenue of all stores during the given time period.
    * @param dateRange
    *   filter the results within this period
    * @return
    *   cumulative revenue
    */
  def cumulativeRevenueDuring(dateTimeRange: DateRange[LocalDateTime]): IO[List[CumulativeRevenue]]

object MoviesReader:
  final case class CumulativeRevenue(
      paymentDate: LocalDate,
      amount: Double,
      cumulativeRevenue: Double,
  )

  object CumulativeRevenue:
    given RowMapper[CumulativeRevenue] = (rows: js.Array[js.Object]) =>
      RowMapper.from[CumulativeRevenue](rows)

  final case class RatingCounter(rating: Rating, count: Int)

  object RatingCounter:
    given RowMapper[RatingCounter] = (rows: js.Array[js.Object]) =>
      RowMapper.from[RatingCounter](rows)

  def impl(transactor: Transactor)(using logger: Logger[IO]): MoviesReader = new MoviesReader:
    override def filmsByRating: IO[List[RatingCounter]] =
      val sql =
        """SELECT rating, count(*) AS count
          | FROM film
          | GROUP BY rating
          |""".stripMargin
      logger.debug(s"Query: $sql") *> transactor.query(sql).list[RatingCounter]

    override def cumulativeRevenueDuring(
        dateTimeRange: DateRange[LocalDateTime],
    ): IO[List[CumulativeRevenue]] =
      val (fromDate, toDate) = dateTimeRange.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME.nn)
      val sql =
        s"""SELECT payment_date AS paymentDate, amount, sum(amount) OVER (ORDER BY payment_date) AS cumulativeRevenue
           | FROM (
           |   SELECT CAST(payment_date AS DATE) AS payment_date, SUM(amount) AS amount
           |   FROM payment
           |   WHERE payment_date BETWEEN '$fromDate' AND '$toDate'
           |   GROUP BY CAST(payment_date AS DATE)
           | ) p
           | ORDER BY payment_date
           |""".stripMargin
      logger.debug(s"Query: $sql") *> transactor.query(sql).list[CumulativeRevenue]
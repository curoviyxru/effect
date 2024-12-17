package moe.crx.effect.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun SqlExpressionBuilder.compareDate(column: Column<Instant>, date: Instant): Op<Boolean> {
    val tz = TimeZone.currentSystemDefault()
    val startDate = date.toLocalDateTime(tz).date
    val endDate = startDate.plus(1, DateTimeUnit.DAY)
    return column.between(startDate.atStartOfDayIn(tz), endDate.atStartOfDayIn(tz))
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
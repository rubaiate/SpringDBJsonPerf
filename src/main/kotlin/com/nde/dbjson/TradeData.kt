package com.nde.dbjson

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

@Entity
class TradeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
    var instrumentId: Int = 0
    var orderBookType: String =""
    var price: BigDecimal = BigDecimal.ZERO
    var time: Instant = Instant.MIN
    var tradeId: String = ""
    var volume: BigDecimal = BigDecimal.ZERO

}
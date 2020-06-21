package com.nde.dbjson

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class TradeDataLoader(repository: TradeDataRepository) {
    init {
        repository.deleteAll()
        val list = generateData(10000)
        repository.saveAll(list)
    }

    companion object{
        fun generateData(size:Int):List<TradeData>{
            val list = mutableListOf<TradeData>()
            repeat(size) { i ->
                val tradeData = TradeData()
                tradeData.instrumentId = i
                tradeData.orderBookType = "Normal"
                tradeData.price = BigDecimal(Math.random())
                tradeData.tradeId = "qwee_$i"
                tradeData.time = Instant.now()
                tradeData.volume = BigDecimal(Math.random())
                list.add(tradeData)
            }
            return list
        }
    }
}
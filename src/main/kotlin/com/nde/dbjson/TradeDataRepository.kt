package com.nde.dbjson

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeDataRepository:CrudRepository<TradeData, Long> {
}
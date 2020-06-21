package com.nde.dbjson

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.hibernate.ScrollMode
import org.hibernate.SessionFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import kotlin.system.measureTimeMillis


@SpringBootTest
class DBJsonTest(@Autowired val tradeDataRepository: TradeDataRepository, @Autowired val dataSource: DataSource, @Autowired val entityManagerFactory: EntityManagerFactory) {

    @Test
    fun loadData() {

        val iterations = 100
        val objectMapper = ObjectMapper()
        val duration = measureTimeMillis {
            repeat(iterations) {
                val list = tradeDataRepository.findAll()
                val out = objectMapper.writeValueAsString(list)
                File("testFile").writeText(out)
            }
        }

        println(duration / iterations)
    }


    @Test
    fun loadDataIterate() {

        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)
        val jfactory = JsonFactory()
        val iterations = 100
        val duration = measureTimeMillis {
            repeat(iterations) {
                val list = tradeDataRepository.findAll()
                val stream = File("testFile").outputStream()
                val jGenerator: JsonGenerator = jfactory
                        .createGenerator(stream, JsonEncoding.UTF8)
                jGenerator.writeStartObject()
                jGenerator.writeFieldName("data")
                jGenerator.writeStartArray()
                list.forEach { objectMapper.writeValue(jGenerator, it) }
                jGenerator.writeEndArray()
                jGenerator.writeEndObject()
                jGenerator.close()
                stream.close()
            }
        }

        println(duration / iterations)
    }

    @Test
    fun loadDataIterateDBConnection() {

        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)
        val jfactory = JsonFactory()
        val iterations = 100
        val tradeData = TradeData()

        val duration = measureTimeMillis {
            repeat(iterations) {

                val conn = dataSource.connection
                conn.use {
                    val statement = conn.createStatement()
                    val resultSet = statement.executeQuery("select * from trade_data")
                    val stream = File("testFile").outputStream()
                    val jGenerator: JsonGenerator = jfactory
                            .createGenerator(stream, JsonEncoding.UTF8)
                    jGenerator.writeStartObject()
                    jGenerator.writeFieldName("data")
                    jGenerator.writeStartArray()
                    while (resultSet.next()) {
                        tradeData.instrumentId = resultSet.getInt(2)
                        tradeData.orderBookType = resultSet.getString(3)
                        tradeData.price = resultSet.getBigDecimal(4)
                        tradeData.time = resultSet.getTimestamp(5).toInstant()
                        tradeData.tradeId = resultSet.getString(6)
                        tradeData.volume = resultSet.getBigDecimal(7)
                        objectMapper.writeValue(jGenerator, tradeData)
                    }
                    jGenerator.writeEndArray()
                    jGenerator.writeEndObject()
                    jGenerator.close()
                    stream.close()
                    statement.close()
                }
            }
        }

        println(duration / iterations)
    }

    @Test
    fun loadDataIterateHibernateStream() {

        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)
        val jfactory = JsonFactory()
        val iterations = 100

        val duration = measureTimeMillis {
            repeat(iterations) {

                val conn = dataSource.connection
                conn.use {
                    val statement = conn.createStatement()
                    val stream = File("testFile").outputStream()
                    val jGenerator: JsonGenerator = jfactory
                            .createGenerator(stream, JsonEncoding.UTF8)
                    jGenerator.writeStartObject()
                    jGenerator.writeFieldName("data")
                    jGenerator.writeStartArray()

                    val sessionFactory: SessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
                    val statelessSession = sessionFactory.openStatelessSession()
                    statelessSession.use {
                        val scrollableResults = it
                                .createQuery("select p from TradeData p")
                                .scroll(ScrollMode.FORWARD_ONLY)

                        scrollableResults.use {
                            while (scrollableResults.next()) {
                                val tradeData: TradeData = scrollableResults[0] as TradeData
                                objectMapper.writeValue(jGenerator, tradeData)
                            }
                        }
                    }

                    jGenerator.writeEndArray()
                    jGenerator.writeEndObject()
                    jGenerator.close()
                    stream.close()
                    statement.close()
                }
            }
        }

        println(duration / iterations)
    }

    @Test
    fun loadDataIterateDBConnectionStreamObject() {

        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)
        val jfactory = JsonFactory()
        val iterations = 100
        val duration = measureTimeMillis {
            repeat(iterations) {

                val conn = dataSource.connection
                conn.use {
                    val statement = conn.createStatement()
                    val resultSet = statement.executeQuery("select * from trade_data")
                    val stream = File("testFile").outputStream()
                    val jGenerator: JsonGenerator = jfactory
                            .createGenerator(stream, JsonEncoding.UTF8)
                    jGenerator.writeStartObject()
                    jGenerator.writeFieldName("data")
                    jGenerator.writeStartArray()
                    while (resultSet.next()) {
                        jGenerator.writeStartObject()
                        jGenerator.writeNumberField("instrumentId", resultSet.getInt(2))
                        jGenerator.writeStringField("orderBookType", resultSet.getString(3))
                        jGenerator.writeNumberField("price", resultSet.getBigDecimal(4))
                        jGenerator.writeNumberField("time", resultSet.getTimestamp(5).time)
                        jGenerator.writeStringField("tradeId", resultSet.getString(6))
                        jGenerator.writeNumberField("volume", resultSet.getBigDecimal(7))
                        jGenerator.writeEndObject()
                    }
                    jGenerator.writeEndArray()
                    jGenerator.writeEndObject()
                    jGenerator.close()
                    stream.close()
                    statement.close()
                }
            }
        }

        println(duration / iterations)
    }
}
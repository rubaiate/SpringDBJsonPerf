package com.nde.dbjson

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.nde.dbjson.TradeDataLoader.Companion.generateData
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.system.measureTimeMillis


class JsonObjectMapperTest {
    @Test
    fun createJsonFileObjectMapper() {
        val list = generateData(10000)

        val objectMapper = ObjectMapper()
        val iterations = 1000
        val duration = measureTimeMillis {
            repeat(iterations) {
                val out = objectMapper.writeValueAsString(list)
                File("testFile").writeText(out)
            }
        }

        println(duration / iterations)
    }

    @Test
    fun createJsonFileFileStream() {
        val list = generateData(10000)

        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)
        val jfactory = JsonFactory()
        val iterations = 1000
        val duration = measureTimeMillis {
            repeat(iterations) {
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
    fun createJsonFileByteArrayOutputStream() {
        val list = generateData(10000)

        val objectMapper = ObjectMapper()
        val jfactory = JsonFactory()
        val iterations = 1000
        val duration = measureTimeMillis {
            repeat(iterations) {
                val stream = ByteArrayOutputStream()
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
                File("testFile").writeBytes(stream.toByteArray())
            }
        }

        println(duration / iterations)
    }
}
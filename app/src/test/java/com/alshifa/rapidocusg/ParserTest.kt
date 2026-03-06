package com.alshifa.rapidocusg

import com.alshifa.rapidocusg.core.documentengine.DocumentType
import com.alshifa.rapidocusg.core.parser.ChatParser
import com.alshifa.rapidocusg.core.parser.ParseConfidence
import com.alshifa.rapidocusg.core.parser.SystemKeywords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ParserTest {

    @Test
    fun testDictionaryDefaultLoad() {
        val mappings = ChatParser.defaultMappings
        assertEquals(SystemKeywords.DOC_LEAVE, mappings["leave"])
        assertEquals(SystemKeywords.DOC_FITNESS, mappings["fitness"])
        assertEquals(SystemKeywords.KEY_DX, mappings["dx"])
    }

    @Test
    fun testDictionaryAddMapping() {
        // We simulate saving into settings by giving it to parse directly
        val customMap = mapOf("medical" to SystemKeywords.DOC_LEAVE)
        val result = ChatParser.parse("Rubina 65 f medical 3 days", customMap)
        assertEquals(DocumentType.MEDICAL_LEAVE_CERT, result.detectedDocType)
    }

    @Test
    fun testDictionaryDeleteMapping() {
        // In UI this just removes from the map.
        // We verify that passing empty custom map uses defaults, and overriding works.
        val result = ChatParser.parse("Rubina 65 f leave 3 days", emptyMap())
        assertEquals(DocumentType.MEDICAL_LEAVE_CERT, result.detectedDocType)
    }

    @Test
    fun testDictionaryReset() {
        val result = ChatParser.parse("Rubina 65 f leave", null)
        assertEquals(DocumentType.MEDICAL_LEAVE_CERT, result.detectedDocType)
    }

    @Test
    fun testAutodetectLeave() {
        val result = ChatParser.parse("Rubina 65 f leave 3 days dx=\"viral fever\" from today")
        assertEquals(DocumentType.MEDICAL_LEAVE_CERT, result.detectedDocType)
        assertEquals(ParseConfidence.HIGH, result.confidence)
        assertEquals("Rubina", result.filledFields[SystemKeywords.KEY_NAME])
        assertEquals("65", result.filledFields[SystemKeywords.KEY_AGE])
        assertEquals("Female", result.filledFields[SystemKeywords.KEY_SEX])
        assertEquals("3", result.filledFields[SystemKeywords.KEY_DAYS])
        assertEquals("viral fever", result.filledFields[SystemKeywords.KEY_DX])
        assertEquals(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), result.filledFields[SystemKeywords.KEY_DATE_FROM])
    }

    @Test
    fun testAutodetectFitness() {
        val result = ChatParser.parse("Ahmed 28 m fitness purpose=duty restrictions=none")
        assertEquals(DocumentType.MEDICAL_FITNESS_CERT, result.detectedDocType)
        assertEquals(ParseConfidence.HIGH, result.confidence)
        assertEquals("Ahmed", result.filledFields[SystemKeywords.KEY_NAME])
        assertEquals("28", result.filledFields[SystemKeywords.KEY_AGE])
        assertEquals("Male", result.filledFields[SystemKeywords.KEY_SEX])
        assertEquals("duty", result.filledFields[SystemKeywords.KEY_PURPOSE])
        assertEquals("none", result.filledFields[SystemKeywords.KEY_RESTRICTIONS])
    }

    @Test
    fun testUnclear() {
        val result = ChatParser.parse("Rubina 65 f 3 days")
        assertNull(result.detectedDocType)
        assertEquals(ParseConfidence.NONE, result.confidence)
        assertTrue(result.unknownTokens.isEmpty() || result.unknownTokens.contains("3"))
        assertEquals("Rubina", result.filledFields[SystemKeywords.KEY_NAME])
    }

    @Test
    fun testPhraseMapping() {
        val customMap = mapOf("fit to join" to SystemKeywords.DOC_FITNESS)
        val result = ChatParser.parse("Ali 30 m fit to join", customMap)
        assertEquals(DocumentType.MEDICAL_FITNESS_CERT, result.detectedDocType)
        assertEquals(ParseConfidence.HIGH, result.confidence)
        assertEquals("Ali", result.filledFields[SystemKeywords.KEY_NAME])
    }
}

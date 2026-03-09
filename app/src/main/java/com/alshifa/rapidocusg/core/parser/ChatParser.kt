package com.alshifa.rapidocusg.core.parser

import com.alshifa.rapidocusg.core.documentengine.DocumentType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

enum class ParseConfidence { HIGH, LOW, NONE }

typealias FieldKey = String

data class ParseResult(
    val detectedDocType: DocumentType?,
    val confidence: ParseConfidence,
    val filledFields: Map<FieldKey, Any>,
    val missingRequired: List<FieldKey>,
    val unknownTokens: List<String>
)

object SystemKeywords {
    const val DOC_USG = "doc_usg"
    const val DOC_LEAVE = "doc_leave"
    const val DOC_FITNESS = "doc_fitness"
    const val DOC_KUB = "doc_kub"
    const val DOC_PELVIS = "doc_pelvis"
    const val DOC_OBSTETRIC = "doc_obstetric"
    const val KEY_NAME = "key_name"
    const val KEY_AGE = "key_age"
    const val KEY_SEX = "key_sex"
    const val KEY_ID = "key_id"
    const val KEY_DX = "key_dx"
    const val KEY_REASON = "key_reason"
    const val KEY_DAYS = "key_days"
    const val KEY_START = "key_start"
    const val KEY_DATE_FROM = "key_date_from"
    const val KEY_DATE_TO = "key_date_to"
    const val KEY_PURPOSE = "key_purpose"
    const val KEY_RESTRICTIONS = "key_restrictions"
    const val KEY_REMARKS = "key_remarks"

    val all = listOf(DOC_USG, DOC_LEAVE, DOC_FITNESS, DOC_KUB, DOC_PELVIS, DOC_OBSTETRIC, KEY_NAME, KEY_AGE, KEY_SEX, KEY_ID, KEY_DX, KEY_REASON, KEY_DAYS, KEY_START, KEY_DATE_FROM, KEY_DATE_TO, KEY_PURPOSE, KEY_RESTRICTIONS, KEY_REMARKS)
}

object ChatParser {
    val defaultMappings = mapOf(
        "leave" to SystemKeywords.DOC_LEAVE,
        "rest" to SystemKeywords.DOC_LEAVE,
        "sick" to SystemKeywords.DOC_LEAVE,
        "fitness" to SystemKeywords.DOC_FITNESS,
        "fit" to SystemKeywords.DOC_FITNESS,
        "abdomen" to SystemKeywords.DOC_USG,
        "usg" to SystemKeywords.DOC_USG,
        "kub" to SystemKeywords.DOC_KUB,
        "pelvis" to SystemKeywords.DOC_PELVIS,
        "obstetric" to SystemKeywords.DOC_OBSTETRIC,
        "obs" to SystemKeywords.DOC_OBSTETRIC,
        "dx" to SystemKeywords.KEY_DX,
        "diagnosis" to SystemKeywords.KEY_DX,
        "reason" to SystemKeywords.KEY_REASON,
        "from" to SystemKeywords.KEY_DATE_FROM,
        "to" to SystemKeywords.KEY_DATE_TO,
        "id" to SystemKeywords.KEY_ID,
        "purpose" to SystemKeywords.KEY_PURPOSE,
        "restrictions" to SystemKeywords.KEY_RESTRICTIONS,
        "remarks" to SystemKeywords.KEY_REMARKS,
        "fit to join" to SystemKeywords.DOC_FITNESS
    )

    fun parse(input: String, customMappings: Map<String, String>? = null): ParseResult {
        val mappings = HashMap(defaultMappings)
        customMappings?.forEach { (k, v) -> mappings[k.lowercase()] = v }
        
        val normalizedInput = input.replace(Regex("\\s+"), " ").trim()
        val tokens = tokenize(normalizedInput)
        
        val replacedTokens = mutableListOf<String>()
        val docTypesFound = mutableSetOf<DocumentType>()
        
        // Phrase matching (greedy longest match first)
        val sortedKeys = mappings.keys.filter { it.contains(" ") }.sortedByDescending { it.length }
        var tsIdx = 0
        while (tsIdx < tokens.size) {
            var matched = false
            for (pKey in sortedKeys) {
                val pTokens = pKey.split(" ")
                if (tsIdx + pTokens.size <= tokens.size) {
                    val slice = tokens.subList(tsIdx, tsIdx + pTokens.size).joinToString(" ").lowercase()
                    if (slice == pKey) {
                        val systemKw = mappings[pKey]!!
                        when (systemKw) {
                            SystemKeywords.DOC_LEAVE -> docTypesFound.add(DocumentType.MEDICAL_LEAVE_CERT)
                            SystemKeywords.DOC_FITNESS -> docTypesFound.add(DocumentType.MEDICAL_FITNESS_CERT)
                            SystemKeywords.DOC_USG -> docTypesFound.add(DocumentType.USG_ABDOMEN)
                            SystemKeywords.DOC_KUB -> docTypesFound.add(DocumentType.USG_KUB)
                            SystemKeywords.DOC_PELVIS -> docTypesFound.add(DocumentType.USG_PELVIS)
                            SystemKeywords.DOC_OBSTETRIC -> docTypesFound.add(DocumentType.USG_OBSTETRIC)
                            else -> replacedTokens.add(systemKw) 
                        }
                        tsIdx += pTokens.size
                        matched = true
                        break
                    }
                }
            }
            if (!matched) {
                replacedTokens.add(tokens[tsIdx])
                tsIdx++
            }
        }

        val filledFields = mutableMapOf<FieldKey, Any>()
        val unknownTokens = mutableListOf<String>()
        var detectedDocType: DocumentType? = null

        var i = 0
        var positionalIndex = 0
        while (i < replacedTokens.size) {
            val token = replacedTokens[i]
            val lowerToken = token.lowercase()

            if (token.contains("=")) {
                val split = token.split("=", limit = 2)
                val k = split[0].lowercase()
                val v = split[1]
                val systemKey = mappings[k] ?: k
                
                when (systemKey) {
                    SystemKeywords.DOC_LEAVE -> docTypesFound.add(DocumentType.MEDICAL_LEAVE_CERT)
                    SystemKeywords.DOC_FITNESS -> docTypesFound.add(DocumentType.MEDICAL_FITNESS_CERT)
                    SystemKeywords.DOC_USG -> docTypesFound.add(DocumentType.USG_ABDOMEN)
                    SystemKeywords.DOC_KUB -> docTypesFound.add(DocumentType.USG_KUB)
                    SystemKeywords.DOC_PELVIS -> docTypesFound.add(DocumentType.USG_PELVIS)
                    SystemKeywords.DOC_OBSTETRIC -> docTypesFound.add(DocumentType.USG_OBSTETRIC)
                    else -> filledFields[systemKey] = parseValue(stripQuotes(v))
                }
                i++
                continue
            }

            val systemKw = mappings[lowerToken]
            if (systemKw != null) {
                when (systemKw) {
                    SystemKeywords.DOC_LEAVE -> docTypesFound.add(DocumentType.MEDICAL_LEAVE_CERT)
                    SystemKeywords.DOC_FITNESS -> docTypesFound.add(DocumentType.MEDICAL_FITNESS_CERT)
                    SystemKeywords.DOC_USG -> docTypesFound.add(DocumentType.USG_ABDOMEN)
                    SystemKeywords.DOC_KUB -> docTypesFound.add(DocumentType.USG_KUB)
                    SystemKeywords.DOC_PELVIS -> docTypesFound.add(DocumentType.USG_PELVIS)
                    SystemKeywords.DOC_OBSTETRIC -> docTypesFound.add(DocumentType.USG_OBSTETRIC)
                    else -> {
                        if (i + 1 < replacedTokens.size) {
                            filledFields[systemKw] = parseValue(stripQuotes(replacedTokens[i+1]))
                            i += 2
                            continue
                        }
                    }
                }
                i++
                continue
            }

            if (lowerToken == "m" || lowerToken == "male") {
                filledFields[SystemKeywords.KEY_SEX] = "Male"
            } else if (lowerToken == "f" || lowerToken == "female") {
                filledFields[SystemKeywords.KEY_SEX] = "Female"
            } else if (lowerToken == "today") {
                filledFields[SystemKeywords.KEY_START] = parseValue(lowerToken)
            } else if (lowerToken == "tomorrow") {
                filledFields[SystemKeywords.KEY_START] = parseValue(lowerToken)
            } else if (isDate(lowerToken)) {
                filledFields[SystemKeywords.KEY_START] = parseValue(lowerToken)
            } else if (lowerToken.matches(Regex("""^x?\d+d$""")) || lowerToken.matches(Regex("""^\d+\s*days$"""))) {
               val num = Regex("""\d+""").find(lowerToken)?.value
               if (num != null) filledFields[SystemKeywords.KEY_DAYS] = num
            } else if (positionalIndex == 0 && !token.any { it.isDigit() }) {
               filledFields[SystemKeywords.KEY_NAME] = stripQuotes(token)
               positionalIndex++
            } else if (positionalIndex == 1 && token.all { it.isDigit() }) {
               filledFields[SystemKeywords.KEY_AGE] = token
               positionalIndex++
            } else {
               unknownTokens.add(token)
            }
            i++
        }

        if (unknownTokens.size >= 2) {
            val durIdx = unknownTokens.indexOfFirst { it.lowercase() == "days" }
            if (durIdx > 0 && unknownTokens[durIdx-1].all { it.isDigit() }) {
                filledFields[SystemKeywords.KEY_DAYS] = unknownTokens[durIdx-1]
                unknownTokens.removeAt(durIdx)
                unknownTokens.removeAt(durIdx-1)
            }
            
            val forIdx = unknownTokens.indexOfFirst { it.lowercase() == "for" }
            if (forIdx >= 0 && forIdx + 2 < unknownTokens.size && unknownTokens[forIdx+1].all { it.isDigit() } && unknownTokens[forIdx+2].lowercase() == "days") {
                filledFields[SystemKeywords.KEY_DAYS] = unknownTokens[forIdx+1]
                unknownTokens.removeAt(forIdx + 2)
                unknownTokens.removeAt(forIdx + 1)
                unknownTokens.removeAt(forIdx)
            }
        }

        if (docTypesFound.size == 1) detectedDocType = docTypesFound.first()
        
        val missing = mutableListOf<String>()
        if (!filledFields.containsKey(SystemKeywords.KEY_NAME)) missing.add(SystemKeywords.KEY_NAME)
        if (!filledFields.containsKey(SystemKeywords.KEY_AGE)) missing.add(SystemKeywords.KEY_AGE)
        if (!filledFields.containsKey(SystemKeywords.KEY_SEX)) missing.add(SystemKeywords.KEY_SEX)

        var confidence = if (docTypesFound.size != 1) ParseConfidence.NONE
                         else if (missing.isNotEmpty()) ParseConfidence.LOW
                         else ParseConfidence.HIGH

        if (detectedDocType == null) confidence = ParseConfidence.NONE

        return ParseResult(detectedDocType, confidence, filledFields, missing, unknownTokens)
    }

    private fun stripQuotes(str: String): String = str.removeSurrounding("\"").removeSurrounding("'")

    private fun parseValue(v: String): String {
        val lower = v.lowercase()
        if (lower == "today") return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (lower == "tomorrow") return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (isDate(lower)) return parseDate(lower) ?: v
        return v
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val matcher = java.util.regex.Pattern.compile("""([^\s"']+)="([^"]*)"|([^\s"']+)=([^\s]+)|"([^"]*)"|'([^']*)'|([^\s]+)""").matcher(input)
        while (matcher.find()) {
            val qv = matcher.group(1) 
            if (qv != null) { tokens.add("$qv=\"${matcher.group(2)}\""); continue }
            
            val nv = matcher.group(3)
            if (nv != null) { tokens.add("$nv=${matcher.group(4)}"); continue }
            
            val q1 = matcher.group(5)
            if (q1 != null) { tokens.add("\"$q1\""); continue }
            
            val q2 = matcher.group(6)
            if (q2 != null) { tokens.add("'$q2'"); continue }
            
            val n = matcher.group(7)
            if (n != null) { tokens.add(n) }
        }
        return tokens
    }

    private fun isDate(s: String): Boolean = s.matches(Regex("""\d{1,2}[/-]\d{1,2}[/-]\d{2,4}"""))
    private fun parseDate(s: String): String? {
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy")
        for (f in formats) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(f)).format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {}
        }
        return null
    }
}

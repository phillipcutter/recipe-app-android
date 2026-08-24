package com.example.notes.data

import com.example.notes.model.Note
import com.example.notes.model.NoteColor

/**
 * A tiny hand-rolled JSON codec for notes.
 *
 * The app deliberately ships with no serialization dependency, and `org.json` is a stub on the
 * JVM unit-test classpath, so encoding lives here in pure Kotlin where it can be tested directly.
 */
object NoteJson {

    fun encode(notes: List<Note>): String =
        notes.joinToString(prefix = "[", postfix = "]") { note ->
            buildString {
                append("{")
                append(field("id", note.id.toString()))
                append(",").append(field("title", quote(note.title)))
                append(",").append(field("body", quote(note.body)))
                append(",").append(field("color", quote(note.color.key)))
                append(",").append(
                    field("tags", note.tags.joinToString(prefix = "[", postfix = "]") { quote(it) })
                )
                append(",").append(field("pinned", note.isPinned.toString()))
                append(",").append(field("archived", note.isArchived.toString()))
                append(",").append(field("createdAt", note.createdAt.toString()))
                append(",").append(field("updatedAt", note.updatedAt.toString()))
                append("}")
            }
        }

    /** Returns an empty list rather than throwing: a corrupt store should not crash launch. */
    fun decode(json: String): List<Note> = runCatching {
        val parsed = Parser(json).parseValue()
        (parsed as? List<*>).orEmpty().mapNotNull { toNote(it) }
    }.getOrDefault(emptyList())

    private fun toNote(value: Any?): Note? {
        val map = value as? Map<*, *> ?: return null
        val id = (map["id"] as? Double)?.toLong() ?: return null
        val created = (map["createdAt"] as? Double)?.toLong() ?: 0L
        return Note(
            id = id,
            title = map["title"] as? String ?: "",
            body = map["body"] as? String ?: "",
            color = NoteColor.fromKey(map["color"] as? String ?: ""),
            tags = (map["tags"] as? List<*>).orEmpty().filterIsInstance<String>().toSet(),
            isPinned = map["pinned"] as? Boolean ?: false,
            isArchived = map["archived"] as? Boolean ?: false,
            createdAt = created,
            updatedAt = (map["updatedAt"] as? Double)?.toLong() ?: created,
        )
    }

    private fun field(name: String, rendered: String) = "${quote(name)}:$rendered"

    private fun quote(raw: String): String = buildString {
        append('"')
        for (char in raw) {
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (char < ' ') append("\\u%04x".format(char.code)) else append(char)
            }
        }
        append('"')
    }

    /** Minimal recursive-descent reader producing Map / List / String / Double / Boolean / null. */
    private class Parser(private val source: String) {
        private var index = 0

        fun parseValue(): Any? {
            skipWhitespace()
            val char = peek()
            return when {
                char == '{' -> parseObject()
                char == '[' -> parseArray()
                char == '"' -> parseString()
                char == 't' -> literal("true", true)
                char == 'f' -> literal("false", false)
                char == 'n' -> literal("null", null)
                char == '-' || char.isDigit() -> parseNumber()
                else -> error("Unexpected character '$char'")
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            val result = LinkedHashMap<String, Any?>()
            skipWhitespace()
            if (peek() == '}') {
                index++
                return result
            }
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                result[key] = parseValue()
                skipWhitespace()
                when (next()) {
                    ',' -> Unit
                    '}' -> return result
                    else -> error("Expected ',' or '}'")
                }
            }
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            val result = mutableListOf<Any?>()
            skipWhitespace()
            if (peek() == ']') {
                index++
                return result
            }
            while (true) {
                result += parseValue()
                skipWhitespace()
                when (next()) {
                    ',' -> Unit
                    ']' -> return result
                    else -> error("Expected ',' or ']'")
                }
            }
        }

        private fun parseString(): String {
            expect('"')
            val builder = StringBuilder()
            while (true) {
                when (val char = next()) {
                    '"' -> return builder.toString()
                    '\\' -> builder.append(unescape(next()))
                    else -> builder.append(char)
                }
            }
        }

        private fun unescape(marker: Char): Char = when (marker) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'b' -> '\u0008'
            'f' -> '\u000C'
            'u' -> source.substring(index, index + 4).toInt(16).toChar().also { index += 4 }
            else -> marker
        }

        private fun parseNumber(): Double {
            val start = index
            while (index < source.length && (
                    source[index] == '-' || source[index] == '+' || source[index] == '.' ||
                        source[index] == 'e' || source[index] == 'E' || source[index].isDigit()
                    )
            ) {
                index++
            }
            return source.substring(start, index).toDouble()
        }

        private fun <T> literal(word: String, value: T): T {
            require(source.startsWith(word, index)) { "Expected $word" }
            index += word.length
            return value
        }

        private fun skipWhitespace() {
            while (index < source.length && source[index].isWhitespace()) index++
        }

        private fun peek(): Char = source[index]

        private fun next(): Char = source[index++]

        private fun expect(char: Char) {
            require(next() == char) { "Expected '$char'" }
        }
    }
}

package excercise

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

data class StringTag(val text: String)

infix fun String.tag(value: String) = this to StringTag(value)

fun renderTemplate(template: String, data: Map<String, StringTag>): String = data.entries
    .fold(template) { acc, (search, replace) ->
        acc.replace("{$search}", replace.text)
    }

class TemplateEngineTest {
    @Test
    fun `can replace template`() {
        val template = """
            Happy Birthday {name} {surname}!
            from {sender}
        """.trimIndent()

        val data = mapOf(
            "name" tag "Peter",
            "surname" tag "Parker",
            "sender" tag "Developers",
        )

        val expected = """
            Happy Birthday Peter Parker!
            from Developers
        """.trimIndent()

        val actual = renderTemplate(template, data)
        expectThat(actual).isEqualTo(expected)
    }
}

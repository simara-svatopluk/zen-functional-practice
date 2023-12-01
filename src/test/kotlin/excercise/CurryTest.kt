package excercise

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun sum(n: Int, m: Int) = n + m
fun concat(s1: String, s2: String) = "$s1 $s2"

fun <A, B, C> ((A, B) -> C).curry(): (A) -> ((B) -> C) = { a ->
    { b -> this(a, b) }
}

fun <A, B, C, D> ((A, B, C) -> D).curry(): (A) -> ((B, C) -> D) = { a ->
    { b, c -> this(a, b, c) }
}

infix fun <A, B> ((A) -> B).`+++`(a: A): B = this(a)

class CurryTest {
    @Test
    fun `curry`() {
        val plus3 = ::sum.curry()(3)
        expectThat(plus3(4)).isEqualTo(7)

        val prefix = ::concat.curry()("*")
        expectThat(prefix("abc")).isEqualTo("* abc")
    }

    @Test
    fun `infix`() {
        val curriedConcat = ::concat.curry()
        expectThat(curriedConcat `+++` "head" `+++` "tail").isEqualTo("head tail")

        val curriedSum = ::sum.curry()
        expectThat(curriedSum `+++` 4 `+++` 5).isEqualTo(9)
    }
}

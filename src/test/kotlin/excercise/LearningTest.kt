package excercise

import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class LearningTest {
    @Test
    fun `0 is an identity element for adding`() = repeat(100) {
        val random = Random.nextInt()
        expectThat(random + 0).isEqualTo(random)
    }

    @Test
    fun `strikt syntax`() {
        expect {
            that(7 + 6).isEqualTo(13)
        }
    }

    @Test
    fun `infix fun`() {
        infix fun Int.next(b: Int): Int {
            return this + b
        }

        expectThat(5 next 6).isEqualTo(11)
    }

    @Test
    fun `then test`() {
        fun a() = 15
        fun b(i: Int) = i + 1
        fun c(i: Int) = i.toString()
        fun d(s: String) = "$s!!!"

        val result = a()
            .let(::b)
            .let(::c)
            .let(::d)

        expectThat(result).isEqualTo("16!!!")

        val composed = ::a let
                ::b let
                ::c let
                ::d

        expectThat(composed()).isEqualTo("16!!!")

        val composedB = ::b let ::c let ::d
        expectThat(composedB(7)).isEqualTo("8!!!")

        val composedOWithoutInput = ::d o ::c o ::b o ::a
        expectThat(composedOWithoutInput()).isEqualTo("16!!!")
    }

    infix fun <A, B> ((A) -> B).o(f: () -> A): () -> B = {
        this(f())
    }

    infix fun <A, B, C> ((B) -> C).o(f: (A) -> B): (A) -> C = { i ->
        this(f(i))
    }

    infix fun <B, C> (() -> B).let(f: (B) -> C): () -> C = { f(this()) }
    infix fun <A, B, C> ((A) -> B).let(f: (B) -> C): (A) -> C = { i -> f(this(i)) }
}


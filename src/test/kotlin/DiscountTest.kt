import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

private fun paidCount(rule: String): (Int) -> Int = { count ->
    val (get, pay) = rule.split("x").map { it.toInt() }

    count.div(get).times(pay) + count.rem(get)
}

private fun cashRegister(
    prices: Map<String, BigDecimal>,
    discounts: Map<String, String>
): (List<String>) -> BigDecimal = { items ->
    groupShoppingList(items)
        .mapValues { (item, count) ->
            paidCount(discounts.getOrDefault(item, "1x1"))(count)
        }.map { (item, paidCount) ->
            prices.getValue(item) * paidCount.toBigDecimal()
        }
        .sumOf { it }
}

private fun groupShoppingList(items: List<String>) = items
    .groupBy { it }
    .mapValues { (_, v) -> v.size }

class DiscountTest {
    private fun randomPrice() = Random.nextDouble()
        .absoluteValue
        .toBigDecimal()
        .times("10".toBigDecimal())
        .round(MathContext(3))

    private fun randomPrices() = mapOf(
        "milk" to randomPrice(),
        "bread" to randomPrice(),
    )

    @Test
    fun `without discount`() {
        val prices = mapOf(
            "milk" to "1.5".toBigDecimal(),
            "bread" to "0.9".toBigDecimal(),
        )

        val discounts = emptyMap<String, String>()

        val result: BigDecimal = cashRegister(prices, discounts)(listOf("milk", "bread"))
        expectThat(result).isEqualTo("2.4".toBigDecimal())
    }

    @Test
    fun `without discount - sums price property`() = repeat(100) {
        val prices = randomPrices()

        val discounts = emptyMap<String, String>()

        val shoppingList = listOf("milk", "bread", "milk", "bread")
        val result: BigDecimal = cashRegister(prices, discounts)(shoppingList)

        expectThat(result).isEqualTo(shoppingList.mapNotNull { prices[it] }.sumOf { it })
    }

    @Test
    fun `apply one discount`() {
        val prices = randomPrices()

        val discounts = mapOf("milk" to "3x2")
        val result = cashRegister(prices, discounts)(List(3) { "milk" })
        expectThat(result).isEqualTo(prices.getValue("milk").times("2".toBigDecimal()))
    }

    @Test
    fun `apply discount - one item - discount applied`() = repeat(100) {
        val prices = randomPrices()

        val discounts = mapOf("milk" to "3x2")
        val countOfMilks = Random.nextInt(1..100)
        val result = cashRegister(prices, discounts)(List(countOfMilks) { "milk" })

        val boundary =
            (countOfMilks.div(3).times(2)..countOfMilks).map { it.toBigDecimal() * prices.getValue("milk") }
        expect {
            that(result).isGreaterThanOrEqualTo(boundary.first())
            that(result).isLessThanOrEqualTo(boundary.last())
        }
    }

    @Test
    fun `can calc paid count for milk - whole discount`() = repeat(100) {
        val rule = paidCount("3x2")
        val count = Random.nextInt(0..100).times(3)

        expectThat(rule(count)).isEqualTo(count.div(3).times(2))
    }

    @Test
    fun `can calc paid count for milk - not whole discount`() = repeat(100) {
        val rule = paidCount("3x2")
        val aboveDiscount = Random.nextInt(1, 2)
        val count = Random.nextInt(0..100).times(3) + aboveDiscount

        expectThat(rule(count)).isEqualTo(count.div(3).times(2) + aboveDiscount)
    }
}

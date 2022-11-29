package machine

import java.lang.reflect.Field
import kotlin.system.exitProcess

const val MSG_INITIAL =
    """The coffee machine has:
%d ml of water
%d ml of milk
%d g of coffee beans
%d disposable cups
${'$'}%d of money"""

const val MSG_WATER_TO_FILL = "Write how many ml of water you want to add:"
const val MSG_MILK_TO_FILL = "Write how many ml of milk you want to add:"
const val MSG_COFFEE_TO_FILL = "Write how many grams of coffee beans you want to add:"
const val MSG_CUPS_TO_FILL = "Write how many disposable cups you want to add:"
const val MSG_CMD_PROMPT = "Write action (buy, fill, take, remaining, exit):"
const val MSG_BUY_PROMPT = "What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino: "

const val MSG_SOLD_SUCCESS = "I have enough resources, making you a coffee!"
const val MSG_RES_LESS = "Sorry, not enough %s!"

fun main() {
    CoffeeMachine().run()
}

open class CoffeeProperties {
    var water: Int
    var milk: Int
    var coffee: Int
    var cup: Int

    constructor(water: Int, milk: Int, coffee: Int, cup: Int = 1) {
        this.water = water
        this.milk = milk
        this.coffee = coffee
        this.cup = cup
    }
}

class CoffeeMachine {
    private var money = 550

    private val stock = CoffeeProperties(400, 540, 120, 9)

    private object Espresso : CoffeeProperties(250, 0, 16, ) {
        const val price = 4
    }

    private object Latte : CoffeeProperties(350, 75, 20, ) {
        const val price = 7
    }

    private object Cappuccino : CoffeeProperties(200, 100, 12, ) {
        const val price = 6
    }

    fun run() {
        do {
            val cmd = getUserInputCMD(MSG_CMD_PROMPT)
            when (cmd) {
                "buy" -> buy()
                "fill" -> fill()
                "take" -> take()
                "remaining" -> remaining()
                "exit" -> exitProcess(1)
            }
        } while (cmd != "exit")
    }

    private fun remaining() {
        println(MSG_INITIAL.format(stock.water, stock.milk, stock.coffee, stock.cup, money))
    }

    private fun fill() {
        stock.water += getUserInputInt(MSG_WATER_TO_FILL)
        stock.milk += getUserInputInt(MSG_MILK_TO_FILL)
        stock.coffee += getUserInputInt(MSG_COFFEE_TO_FILL)
        stock.cup += getUserInputInt(MSG_CUPS_TO_FILL)
    }

    private fun take() {
        println("I gave you \$$money\n")
        money = 0
    }

    private fun buy() {
        val cmd = getUserInputInt(MSG_BUY_PROMPT)

        if (cmd == 0)
            return
        else {
            val (coffeeFormula, price) = when (cmd) {
                1 -> Espresso to Espresso.price
                2 -> Latte to Latte.price
                3 -> Cappuccino to Cappuccino.price
                else -> throw Exception("Wrong coffee type")
            }
            if (makingCoffee(coffeeFormula)) {
                money += price
                println(MSG_SOLD_SUCCESS)
            }
        }
    }

    private fun makingCoffee(formula: CoffeeProperties): Boolean {
        for (field in CoffeeProperties::class.java.declaredFields) {
            field.isAccessible = true
            val stockValue = field.get(stock) as Int
            val formulaValue = field.get(formula) as Int
            if (stockValue - formulaValue > 0) {
                field.set(stock, stockValue - formulaValue)
            } else {
                println(MSG_RES_LESS.format(field.name))
                return false
            }
        }
        return true
    }
}

fun getUserInputInt(msg: String): Int {
    while (true) {
        println(msg)
        val cmd = readln()
        return if (cmd == "back") 0 else cmd?.toIntOrNull() ?: continue
    }
}

fun getUserInputCMD(msg: String): String {
    while (true) {
        println(msg)
        val cmd = readln()
        return if ("\\((.+)\\)".toRegex().find(MSG_CMD_PROMPT)!!.groupValues[1].split(", ")
                .contains(cmd)
        ) cmd else continue
    }
}

package de.jvstvshd.foxesbot.module.christmas

import dev.kord.rest.builder.message.EmbedBuilder
import java.util.*

object BigRegeneration : HpEvent(0.08) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.HP, startHp - 48)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Große Regeneration"
    }
}

object BigDegeneration : HpEvent(0.07) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.HP, startHp - 14)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Große Degeneration"
    }
}

object SmallRegeneration : HpEvent(0.15) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.HP, startHp + 23)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Kleine Regeneration"
    }
}

object SmallDegeneration : HpEvent(0.1) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.HP, startHp + 7)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Kleine Degeneration"
    }
}

object SnowballGain : HpEvent(0.3) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.SNOWBALL_GAIN)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Schneebälle"
    }
}

object LimitExpansion : HpEvent(0.05) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.LIMIT_EXPANSION)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Limit-Erhöhung"
    }
}

object Nothing : HpEvent(0.25) {
    override fun execute(startHp: Int): EventResult = EventResult(Type.NOTHING)
    override fun sendMessage(builder: EmbedBuilder) {
        builder.description = "Nichts"
    }
}

abstract class HpEvent(val probability: Double) {
    companion object {
        private val list = listOf(
            BigDegeneration,
            BigRegeneration,
            SmallRegeneration,
            SmallDegeneration,
            SnowballGain,
            LimitExpansion,
            Nothing
        )

        private val randomSelector = RandomSelector(list)

        fun pickRandom() = randomSelector.getRandom()

    }

    abstract fun execute(startHp: Int): EventResult

    abstract fun sendMessage(builder: EmbedBuilder)
}

private class RandomSelector(private val items: List<HpEvent>) {
    val rand = Random()
    var totalSum = 0.0

    init {
        for (item in items) {
            totalSum += item.probability
        }
    }

    fun getRandom(): HpEvent {
        val index = rand.nextDouble(totalSum)
        var sum = 0.0
        var i = 0
        while (sum < index) {
            sum += items[i++].probability
        }
        return items[0.coerceAtLeast(i - 1)]
    }

}

data class EventResult(val type: Type, val newHp: Int = 0)

enum class Type {
    HP,
    SNOWBALL_GAIN,
    LIMIT_EXPANSION,
    NOTHING
}
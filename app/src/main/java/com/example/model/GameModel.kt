package com.example.model

import java.util.UUID

enum class CardColor(val displayName: String) {
    RED("Rojo"),
    GREEN("Verde"),
    BLUE("Azul"),
    YELLOW("Amarillo"),
    WILD("Negro")
}

enum class CardType {
    NUMBER,
    PLUS_1,
    PLUS_2,
    PLUS_5,
    PLUS_10,
    PLUS_20,
    PLUS_50,
    SKIP,
    DOUBLE_SKIP,
    REVERSE,
    WILD,
    WILD_PLUS_4
}

data class Card(
    val id: String = UUID.randomUUID().toString(),
    val color: CardColor,
    val type: CardType,
    val number: Int? = null, // Used for NUMBER card types
    val value: Int = 0       // General draw value or action representation
) {
    val label: String
        get() = when (type) {
            CardType.NUMBER -> number?.toString() ?: "0"
            CardType.PLUS_1 -> "+1"
            CardType.PLUS_2 -> "+2"
            CardType.PLUS_5 -> "+5"
            CardType.PLUS_10 -> "+10"
            CardType.PLUS_20 -> "+20"
            CardType.PLUS_50 -> "+50"
            CardType.SKIP -> "🚫 Bloqueo"
            CardType.DOUBLE_SKIP -> "🚫🚫 Doble Bloqueo"
            CardType.REVERSE -> "🔄 Retorno"
            CardType.WILD -> "🌈 Comodín"
            CardType.WILD_PLUS_4 -> "🌈 +4"
        }

    fun isCompatibleWith(other: Card, activeColor: CardColor): Boolean {
        // Wild cards are always compatible
        if (this.color == CardColor.WILD) return true
        
        // Colors match
        if (this.color == activeColor) return true
        
        // Same type matches (e.g. both are SKIP or both are PLUS_10)
        if (this.type == other.type) {
            if (this.type == CardType.NUMBER) {
                return this.number == other.number
            }
            return true
        }
        
        return false
    }
}

data class Player(
    val id: String,
    val name: String,
    val isBot: Boolean = true,
    val hand: List<Card> = emptyList(),
    val speechBubble: String? = null,
    val freezeTurns: Int = 0,         // Double Skip or Judge Frozen turns
    val hasJudgeFavor: Boolean = false, // True if declared in advantage by Juez
    val avatarEmoji: String = "🤖"
)

enum class GameMode {
    MENU,
    CLASSIC_PLAY, // 4 Players (Player vs 3 Smart Bots: Carlos, Ana, Pedro)
    JUDGE_PLAY,   // Spectator/Judge mode of Carlos vs Ana (1v1)
    RULES
}

enum class JudgeCardType {
    RAIN_OF_CARDS_10,   // Chosen bot draws 10 cards
    EARTHQUAKE_SWAP,    // Swap hands between bots!
    ABSOLUTE_FREEZE,    // Target bot frozen for 2 turns
    CARD_CLEANSE_3,     // Help a bot by discarding 3 random cards
    COLOR_TERREMOTO,    // Force a color change of the game card to Judge's choice
    FORCED_CHARITY       // Force bot with fewer cards to give their best card to the bot with more cards
}

data class JudgeCard(
    val id: String = UUID.randomUUID().toString(),
    val type: JudgeCardType,
    val name: String,
    val description: String,
    val emoji: String
)

data class GameLog(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isImportant: Boolean = false,
    val timestamp: String
)

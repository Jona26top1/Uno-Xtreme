package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UnoViewModel : ViewModel() {

    // Main Game Mode
    private val _gameMode = MutableStateFlow(GameMode.MENU)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    // Active players in current game
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    // Top card on discard pile
    private val _topCard = MutableStateFlow<Card?>(null)
    val topCard: StateFlow<Card?> = _topCard.asStateFlow()

    // Current active color (important because WILD cards change the color)
    private val _activeColor = MutableStateFlow(CardColor.RED)
    val activeColor: StateFlow<CardColor> = _activeColor.asStateFlow()

    // Turn control
    private val _currentTurnIndex = MutableStateFlow(0)
    val currentTurnIndex: StateFlow<Int> = _currentTurnIndex.asStateFlow()

    private val _turnDirection = MutableStateFlow(1) // 1 for normal, -1 for reverse
    val turnDirection: StateFlow<Int> = _turnDirection.asStateFlow()

    private val _isBotThinking = MutableStateFlow(false)
    val isBotThinking: StateFlow<Boolean> = _isBotThinking.asStateFlow()

    private val _thinkingPlayerId = MutableStateFlow<String?>(null)
    val thinkingPlayerId: StateFlow<String?> = _thinkingPlayerId.asStateFlow()

    // Discard and draw piles
    private var drawPile = mutableListOf<Card>()
    private val discardPile = mutableListOf<Card>()

    // Game Logs
    private val _logs = MutableStateFlow<List<GameLog>>(emptyList())
    val logs: StateFlow<List<GameLog>> = _logs.asStateFlow()

    // Judge Mode elements
    private val _judgeHand = MutableStateFlow<List<JudgeCard>>(emptyList())
    val judgeHand: StateFlow<List<JudgeCard>> = _judgeHand.asStateFlow()

    private val _selectedJudgeWinner = MutableStateFlow<String?>(null) // ID of bot with hand advantage declared by the user
    val selectedJudgeWinner: StateFlow<String?> = _selectedJudgeWinner.asStateFlow()

    // Sound and custom notifications state triggers
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    // Game winner
    private val _winner = MutableStateFlow<Player?>(null)
    val winner: StateFlow<Player?> = _winner.asStateFlow()

    // Selected wild color selection dialog
    private val _showWildDialog = MutableStateFlow(false)
    val showWildDialog: StateFlow<Boolean> = _showWildDialog.asStateFlow()
    private var pendingWildCard: Card? = null

    // Coroutine job managing automatic bot turns
    private var botTurnJob: Job? = null

    init {
        resetGame()
    }

    fun setGameMode(mode: GameMode) {
        _gameMode.value = mode
        stopBotTurns()
        _winner.value = null
        if (mode == GameMode.CLASSIC_PLAY) {
            setupClassicGame()
        } else if (mode == GameMode.JUDGE_PLAY) {
            setupJudgeGame()
        }
    }

    private fun addLog(message: String, isImportant: Boolean = false) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val newLog = GameLog(message = message, isImportant = isImportant, timestamp = timeStr)
        _logs.value = listOf(newLog) + _logs.value.take(49) // Keep last 50 logs
    }

    private fun stopBotTurns() {
        botTurnJob?.cancel()
        botTurnJob = null
        _isBotThinking.value = false
        _thinkingPlayerId.value = null
    }

    private fun resetGame() {
        stopBotTurns()
        _players.value = emptyList()
        _topCard.value = null
        _currentTurnIndex.value = 0
        _turnDirection.value = 1
        _logs.value = emptyList()
        _winner.value = null
        _selectedJudgeWinner.value = null
        drawPile.clear()
        discardPile.clear()
    }

    // Master list generation
    private fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        val colors = listOf(CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW)

        for (color in colors) {
            // Numbers 0 to 9
            deck.add(Card(color = color, type = CardType.NUMBER, number = 0, value = 0))
            for (i in 1..9) {
                deck.add(Card(color = color, type = CardType.NUMBER, number = i, value = i))
                deck.add(Card(color = color, type = CardType.NUMBER, number = i, value = i))
            }

            // Standard skips and reverses
            deck.add(Card(color = color, type = CardType.SKIP, value = 20))
            deck.add(Card(color = color, type = CardType.SKIP, value = 20))
            deck.add(Card(color = color, type = CardType.REVERSE, value = 20))
            deck.add(Card(color = color, type = CardType.REVERSE, value = 20))

            // ADVANTAGES: Custom '+' and Double Skip cards per color!
            deck.add(Card(color = color, type = CardType.PLUS_1, value = 1))
            deck.add(Card(color = color, type = CardType.PLUS_1, value = 1))
            deck.add(Card(color = color, type = CardType.PLUS_2, value = 2))
            deck.add(Card(color = color, type = CardType.PLUS_2, value = 2))
            deck.add(Card(color = color, type = CardType.PLUS_5, value = 5))
            deck.add(Card(color = color, type = CardType.PLUS_10, value = 10))
            deck.add(Card(color = color, type = CardType.PLUS_20, value = 20))
            deck.add(Card(color = color, type = CardType.PLUS_50, value = 50)) // Brutal advantages!
            deck.add(Card(color = color, type = CardType.DOUBLE_SKIP, value = 40)) // Custom block
        }

        // Wildcards (Black)
        repeat(4) {
            deck.add(Card(color = CardColor.WILD, type = CardType.WILD, value = 50))
            deck.add(Card(color = CardColor.WILD, type = CardType.WILD_PLUS_4, value = 50))
        }

        deck.shuffle()
        return deck
    }

    private fun ensureDrawPileNotEmpty(requiredCount: Int = 1) {
        if (drawPile.size < requiredCount + 5) {
            val oldTop = _topCard.value
            val recycled = discardPile.filter { it.id != oldTop?.id }.shuffled()
            drawPile.addAll(recycled)
            discardPile.removeAll(recycled)
            addLog("El mazo se estaba agotando. Se mezclaron ${recycled.size} cartas descartadas.", isImportant = true)
        }
    }

    private fun setupClassicGame() {
        resetGame()
        drawPile = createDeck()

        val botNames = listOf("Carlos", "Ana", "Pedro")
        val botEmojis = listOf("🦊", "🙋‍♀️", "🦁")

        // Draw 7 cards for each player
        val playerHand = mutableListOf<Card>()
        repeat(7) { playerHand.add(drawPile.removeAt(0)) }

        val initialPlayers = mutableListOf<Player>()
        initialPlayers.add(Player(id = "user", name = "Jugador (Tú)", isBot = false, hand = playerHand, avatarEmoji = "😎"))

        for (i in botNames.indices) {
            val botHand = mutableListOf<Card>()
            repeat(7) { botHand.add(drawPile.removeAt(0)) }
            initialPlayers.add(
                Player(
                    id = "bot_$i",
                    name = botNames[i],
                    isBot = true,
                    hand = botHand,
                    avatarEmoji = botEmojis[i]
                )
            )
        }

        _players.value = initialPlayers

        // Draw initial top card (must be a number card for clean setup)
        var firstCardIndex = drawPile.indexOfFirst { it.color != CardColor.WILD && it.type == CardType.NUMBER }
        if (firstCardIndex == -1) firstCardIndex = 0
        val startingCard = drawPile.removeAt(firstCardIndex)
        discardPile.add(startingCard)

        _topCard.value = startingCard
        _activeColor.value = startingCard.color
        _currentTurnIndex.value = 0

        addLog("¡Inicia Partida Clásica! Carta inicial: ${startingCard.label} (${startingCard.color.displayName})", isImportant = true)
        triggerSpeakerBubble("bot_1", "¡Prepárense, bots y humanos, voy a ganar!") // Ana (bot_1) comments initially!
    }

    private fun setupJudgeGame() {
        resetGame()
        drawPile = createDeck()

        // 1v1 setup
        val botNames = listOf("Carlos", "Ana")
        val botEmojis = listOf("🦊", "🙋‍♀️")

        val initialPlayers = mutableListOf<Player>()
        for (i in botNames.indices) {
            val botHand = mutableListOf<Card>()
            repeat(7) { botHand.add(drawPile.removeAt(0)) }
            initialPlayers.add(
                Player(
                    id = "bot_$i",
                    name = botNames[i],
                    isBot = true,
                    hand = botHand,
                    avatarEmoji = botEmojis[i]
                )
            )
        }
        _players.value = initialPlayers

        // Start card
        var firstCardIndex = drawPile.indexOfFirst { it.color != CardColor.WILD && it.type == CardType.NUMBER }
        if (firstCardIndex == -1) firstCardIndex = 0
        val startingCard = drawPile.removeAt(firstCardIndex)
        discardPile.add(startingCard)

        _topCard.value = startingCard
        _activeColor.value = startingCard.color
        _currentTurnIndex.value = 0

        // Create initial judge cards
        val initialJudgeHand = listOf(
            JudgeCard(type = JudgeCardType.RAIN_OF_CARDS_10, name = "Lluvia +10", description = "El bot objetivo roba 10 cartas.", emoji = "🌧️"),
            JudgeCard(type = JudgeCardType.EARTHQUAKE_SWAP, name = "Terremoto", description = "Intercambia las barajas de Carlos y Ana.", emoji = "🌋"),
            JudgeCard(type = JudgeCardType.ABSOLUTE_FREEZE, name = "Congelar (2T)", description = "Sáltale 2 turnos seguidos al bot objetivo.", emoji = "❄️"),
            JudgeCard(type = JudgeCardType.CARD_CLEANSE_3, name = "Limpieza Exprés", description = "Elimina 3 cartas aleatorias de un bot.", emoji = "🧹"),
            JudgeCard(type = JudgeCardType.COLOR_TERREMOTO, name = "Golpe de Color", description = "Cambia el color de juego a tu antojo.", emoji = "🎨"),
            JudgeCard(type = JudgeCardType.FORCED_CHARITY, name = "Donación Forzada", description = "El bot con menos cartas entrega su mejor carta al otro.", emoji = "🤝")
        )
        _judgeHand.value = initialJudgeHand

        addLog("¡Inicia el Modo Juez! Carlos 🦊 vs Ana 🙋‍♀️. TÚ tienes el control supremo.", isImportant = true)
        triggerSpeakerBubble("bot_1", "¡Juez, vigila a Carlos, seguro esconde cartas!") // Ana talks!
        triggerSpeakerBubble("bot_0", "¡Siento que el Juez me favorecerá hoy!") // Carlos talks!

        // Instantly kickoff automatic loop
        checkAndTriggerBotTurn()
    }

    // Toggle declared judge advantage
    fun declareJudgeWinner(playerId: String?) {
        _selectedJudgeWinner.value = playerId
        val updatedList = _players.value.map { p ->
            p.copy(hasJudgeFavor = (p.id == playerId))
        }
        _players.value = updatedList

        if (playerId != null) {
            val favoredPlayer = updatedList.find { it.id == playerId }
            val unfavoredPlayer = updatedList.find { it.id != playerId }
            addLog("⚖️ Juez declara Ventaja para: ${favoredPlayer?.name}. Obtiene un Escudo del Juez.", isImportant = true)
            triggerSpeakerBubble(playerId, "¡Sí! El Juez reconoce mi superioridad táctica 😎")
            unfavoredPlayer?.let { triggerSpeakerBubble(it.id, "¡Qué injusticia, Juez! Claramente hay favoritismo... 😡") }
        } else {
            addLog("⚖️ El Juez restablece la balanza a Neutro.", isImportant = true)
        }
    }

    // Cast a judge card on a specific target bot
    fun playJudgeCard(judgeCard: JudgeCard, targetBotId: String?) {
        val opponentId = _players.value.find { it.id != targetBotId }?.id
        val targetName = _players.value.find { it.id == targetBotId }?.name ?: "Nadie"

        when (judgeCard.type) {
            JudgeCardType.RAIN_OF_CARDS_10 -> {
                if (targetBotId == null) return
                drawCardsForPlayer(targetBotId, 10, forcedByJudge = true)
                addLog("⚖️ JUEZ usó [${judgeCard.name}] sobre $targetName: ¡Robó 10 cartas!", isImportant = true)
                triggerSpeakerBubble(targetBotId, "¡¿10 CARTAS?! ¡Esto es abuso de poder, señor Juez! 😭")
            }
            JudgeCardType.EARTHQUAKE_SWAP -> {
                if (_players.value.size < 2) return
                val p1 = _players.value[0]
                val p2 = _players.value[1]
                val tempHand = p1.hand
                _players.value = _players.value.map { p ->
                    if (p.id == p1.id) p.copy(hand = p2.hand)
                    else p.copy(hand = tempHand)
                }
                addLog("⚖️ JUEZ desató un [TERREMOTO]: ¡Se han intercambiado las barajas de Carlos y Ana!", isImportant = true)
                triggerSpeakerBubble(p1.id, "¡Oye, mis hermosas cartas! 😡")
                triggerSpeakerBubble(p2.id, "¡Ja, ja! Me encantan tus nuevas cartas.")
            }
            JudgeCardType.ABSOLUTE_FREEZE -> {
                if (targetBotId == null) return
                _players.value = _players.value.map { p ->
                    if (p.id == targetBotId) p.copy(freezeTurns = 2) else p
                }
                addLog("⚖️ JUEZ usó [${judgeCard.name}]: $targetName queda congelado por 2 turnos.", isImportant = true)
                triggerSpeakerBubble(targetBotId, "¡No puedo moverme! ¡Fui saboteado por el Juez! ❄️")
            }
            JudgeCardType.CARD_CLEANSE_3 -> {
                if (targetBotId == null) return
                val bot = _players.value.find { it.id == targetBotId } ?: return
                val countToRemove = minOf(3, bot.hand.size)
                if (countToRemove > 0) {
                    val newHand = bot.hand.shuffled().drop(countToRemove)
                    _players.value = _players.value.map { p ->
                        if (p.id == targetBotId) p.copy(hand = newHand) else p
                    }
                    addLog("⚖️ JUEZ usó [${judgeCard.name}]: Retiró $countToRemove cartas de $targetName.", isImportant = true)
                    triggerSpeakerBubble(targetBotId, "¡Ahhh, un alivio limpio! ¡Gracias por el favor celestial, Juez! ✨")
                }
            }
            JudgeCardType.COLOR_TERREMOTO -> {
                // Randomly trigger standard change color cycle
                val randomColors = CardColor.values().filter { it != CardColor.WILD }
                val chosenColor = randomColors.random()
                _activeColor.value = chosenColor
                addLog("⚖️ JUEZ decretó [${judgeCard.name}]: Cambió el color de juego a ${chosenColor.displayName}.", isImportant = true)
                _players.value.forEach { p ->
                    triggerSpeakerBubble(p.id, "¡El Juez cambió el color a ${chosenColor.displayName}! Reacomodando estrategia.")
                }
            }
            JudgeCardType.FORCED_CHARITY -> {
                if (_players.value.size < 2) return
                val c1 = _players.value[0]
                val c2 = _players.value[1]
                if (c1.hand.isEmpty() || c2.hand.isEmpty()) return

                val (richBot, poorBot) = if (c1.hand.size > c2.hand.size) Pair(c1, c2) else Pair(c2, c1)
                // poorBot must give their best card (or just a random card for simplicity) to richBot
                val charityCard = poorBot.hand.random()
                _players.value = _players.value.map { p ->
                    if (p.id == poorBot.id) {
                        p.copy(hand = p.hand.filter { it.id != charityCard.id })
                    } else if (p.id == richBot.id) {
                        p.copy(hand = p.hand + charityCard)
                    } else {
                        p
                    }
                }
                addLog("⚖️ JUEZ decretó [Donación Forzada]: ${poorBot.name} debió darle su ${charityCard.label} (color ${charityCard.color.displayName}) a ${richBot.name}.", isImportant = true)
                triggerSpeakerBubble(poorBot.id, "¡Me obligaron a donar! ¡Qué día tan triste! 😭")
                triggerSpeakerBubble(richBot.id, "¡Muchas gracias por la caridad caritativa, humilde bot! 🎁")
            }
        }

        // Spend the used card from judge hand
        _judgeHand.value = _judgeHand.value.filter { it.id != judgeCard.id }

        // Regenerate card automatically if hand got low (so the judge never runs out of influence)
        if (_judgeHand.value.size < 3) {
            val possibleGifts = listOf(
                JudgeCard(type = JudgeCardType.RAIN_OF_CARDS_10, name = "Lluvia +10", description = "El bot objetivo roba 10 cartas.", emoji = "🌧️"),
                JudgeCard(type = JudgeCardType.EARTHQUAKE_SWAP, name = "Terremoto", description = "Intercambia las barajas de Carlos y Ana.", emoji = "🌋"),
                JudgeCard(type = JudgeCardType.ABSOLUTE_FREEZE, name = "Congelar (2T)", description = "Sáltale 2 turnos seguidos al bot objetivo.", emoji = "❄️"),
                JudgeCard(type = JudgeCardType.CARD_CLEANSE_3, name = "Limpieza Exprés", description = "Elimina 3 cartas aleatorias de un bot.", emoji = "🧹"),
                JudgeCard(type = JudgeCardType.COLOR_TERREMOTO, name = "Golpe de Color", description = "Cambia el color de juego a tu antojo.", emoji = "🎨"),
                JudgeCard(type = JudgeCardType.FORCED_CHARITY, name = "Donación Forzada", description = "El bot con menos cartas entrega su mejor carta al otro.", emoji = "🤝")
            ).shuffled()
            _judgeHand.value = _judgeHand.value + possibleGifts.take(2)
        }

        // Verify if color change triggered any state updates
        verifyGameWinnerState()
        checkAndTriggerBotTurn()
    }

    // Helper to trigger dialogue bubble with timer auto-dismiss
    fun triggerSpeakerBubble(playerId: String, text: String) {
        _players.value = _players.value.map { p ->
            if (p.id == playerId) p.copy(speechBubble = text) else p
        }
        viewModelScope.launch {
            delay(3500)
            _players.value = _players.value.map { p ->
                if (p.id == playerId && p.speechBubble == text) p.copy(speechBubble = null) else p
            }
        }
    }

    // Classic game actions
    fun userDrawCard() {
        if (_gameMode.value != GameMode.CLASSIC_PLAY) return
        if (isUserTurn()) {
            ensureDrawPileNotEmpty(1)
            val drawn = drawPile.removeAt(0)
            val updatedPlayers = _players.value.map { p ->
                if (p.id == "user") p.copy(hand = p.hand + drawn) else p
            }
            _players.value = updatedPlayers
            addLog("Tú robaste una carta: ${drawn.label} de color ${drawn.color.displayName}.")

            // If the card is playable immediately, allow user to play it or manually pass.
            // For a smooth flow, if there's no playable card, advance turn automatically after a short delay.
            val canPlayDrawn = drawn.isCompatibleWith(_topCard.value!!, _activeColor.value)
            if (!canPlayDrawn) {
                viewModelScope.launch {
                    delay(800)
                    advanceTurn()
                }
            }
        }
    }

    fun userPlayCard(card: Card, targetColor: CardColor? = null) {
        if (_gameMode.value != GameMode.CLASSIC_PLAY) return
        if (!isUserTurn()) return

        val tc = _topCard.value ?: return
        if (!card.isCompatibleWith(tc, _activeColor.value)) {
            _notification.value = "¡Esa carta no se puede jugar!"
            viewModelScope.launch {
                delay(1500)
                _notification.value = null
            }
            return
        }

        executeCardPlay("user", card, targetColor)
    }

    private fun isUserTurn(): Boolean {
        val list = _players.value
        if (_currentTurnIndex.value in list.indices) {
            return list[_currentTurnIndex.value].id == "user"
        }
        return false
    }

    // Execute card from hand
    private fun executeCardPlay(playerId: String, card: Card, chosenWildColor: CardColor? = null) {
        stopBotTurns()

        val player = _players.value.find { it.id == playerId } ?: return

        // 1. Remove from hand
        val updatedHand = player.hand.filter { it.id != card.id }
        _players.value = _players.value.map { p ->
            if (p.id == playerId) p.copy(hand = updatedHand) else p
        }

        // 2. Add to discard pile
        discardPile.add(card)
        _topCard.value = card

        // 3. Resolve wildcard or normal colors
        if (card.color == CardColor.WILD) {
            if (player.isBot) {
                // Smart color picking for bot
                val smartColor = determineSmartColorForBot(player)
                _activeColor.value = smartColor
                addLog("${player.name} jugó ${card.label} y cambió el color a ${smartColor.displayName}.", isImportant = true)
            } else {
                // For user play, expect chosenWildColor
                val finalColor = chosenWildColor ?: CardColor.RED
                _activeColor.value = finalColor
                addLog("Jugaste ${card.label} y designaste el color ${finalColor.displayName}.", isImportant = true)
            }
        } else {
            _activeColor.value = card.color
            addLog("${player.name} jugó ${card.label} (${card.color.displayName}).")
        }

        // 4. Shout "UNO" if 1 card left
        if (updatedHand.size == 1) {
            addLog("📣 ¡${player.name} grita uno!", isImportant = true)
            triggerSpeakerBubble(player.id, "¡¡UNO!! 📣")
        }

        // 5. Apply action effects immediately
        var stepsToAdvance = _turnDirection.value
        var blockTriggered = false
        var hitPenalty = 0
        var skipMessage = ""

        when (card.type) {
            CardType.SKIP -> {
                stepsToAdvance *= 2
                blockTriggered = true
                skipMessage = "¡Se saltó el turno del siguiente jugador!"
            }
            CardType.DOUBLE_SKIP -> {
                // "Doble Bloqueo" skips two players! So we move 3 positions!
                stepsToAdvance *= 3
                blockTriggered = true
                skipMessage = "🚫🚫 ¡Bloqueo Doble! Se saltaron los turnos de los siguientes DOS jugadores."
                
                // Set speech bubble trigger
                triggerSpeakerBubble(playerId, "¡Bloqueo Doble! ¡Sáltense dos!")
            }
            CardType.REVERSE -> {
                val newDir = _turnDirection.value * -1
                _turnDirection.value = newDir
                addLog("🔄 Se invirtió el orden del juego.", isImportant = true)
                stepsToAdvance = newDir // Align next steps instantly
            }
            CardType.PLUS_1 -> hitPenalty = 1
            CardType.PLUS_2 -> hitPenalty = 2
            CardType.PLUS_5 -> hitPenalty = 5
            CardType.PLUS_10 -> hitPenalty = 10
            CardType.PLUS_20 -> hitPenalty = 20
            CardType.PLUS_50 -> hitPenalty = 50 // Ultimate mega deck draw advantage!
            CardType.WILD_PLUS_4 -> hitPenalty = 4
            else -> {}
        }

        if (blockTriggered) {
            addLog(skipMessage, isImportant = true)
        }

        // Verify if a win was achieved
        if (verifyGameWinnerState()) {
            return
        }

        // 6. If action card was a draw card (+N), apply payload to the next target
        if (hitPenalty > 0) {
            val targetPlayerIndex = getNextActivePlayerIndex(1)
            val targetPlayer = _players.value[targetPlayerIndex]

            // Apply draw payload
            drawCardsForPlayer(targetPlayer.id, hitPenalty)

            // Let target speak funny defensive/disbelief quotes!
            val shockQuote = when {
                hitPenalty >= 50 -> "¡¿MÁS CINCUENTA?! ¡Eso destruye mi vida entera! 😭🥀"
                hitPenalty >= 10 -> "¡+${hitPenalty}! ¡Juez, arréstelo por crueldad animal! 💀"
                else -> "¡Auu, robaré $hitPenalty exactas!"
            }
            triggerSpeakerBubble(targetPlayer.id, shockQuote)

            // Skip the penalized player's turn as per general +N rules
            stepsToAdvance = _turnDirection.value * 2
        }

        // 7. Advance turn index
        advanceTurnBy(stepsToAdvance)
    }

    private fun verifyGameWinnerState(): Boolean {
        for (p in _players.value) {
            if (p.hand.isEmpty()) {
                _winner.value = p
                addLog("👑 ¡PARTIDA CONCLUIDA! El triunfador de hoy es ${p.name}! Felicitaciones.", isImportant = true)
                triggerSpeakerBubble(p.id, "¡SÍÍÍ! ¡Gané limpia y justamente! 🏆👑")
                stopBotTurns()
                return true
            }
        }
        return false
    }

    // Smart color picker based on max cards of a specific color in bot hand
    private fun determineSmartColorForBot(bot: Player): CardColor {
        val counts = mutableMapOf<CardColor, Int>()
        for (card in bot.hand) {
            if (card.color != CardColor.WILD) {
                counts[card.color] = (counts[card.color] ?: 0) + 1
            }
        }
        return counts.maxByOrNull { it.value }?.key ?: listOf(CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW).random()
    }

    // Fetch next player index considering circular rotation
    private fun getNextActivePlayerIndex(stepsCount: Int): Int {
        val total = _players.value.size
        if (total == 0) return 0
        val currentIdx = _currentTurnIndex.value
        val movement = stepsCount * _turnDirection.value
        var nextIdx = (currentIdx + movement) % total
        if (nextIdx < 0) {
            nextIdx += total
        }
        return nextIdx
    }

    private fun advanceTurnBy(steps: Int) {
        val total = _players.value.size
        if (total == 0) return
        var nextIdx = (_currentTurnIndex.value + steps) % total
        if (nextIdx < 0) {
            nextIdx += total
        }

        _currentTurnIndex.value = nextIdx

        // Check for Frozen Target (frozen turns skip active action)
        val candidate = _players.value[nextIdx]
        if (candidate.freezeTurns > 0) {
            // Decrement freeze turn, notify log and skip
            val updated = _players.value.map { p ->
                if (p.id == candidate.id) p.copy(freezeTurns = candidate.freezeTurns - 1) else p
            }
            _players.value = updated
            addLog("❄️ El turno de ${candidate.name} fue omitido debido a que está congelado (Quedan: ${candidate.freezeTurns - 1} turnos).")
            triggerSpeakerBubble(candidate.id, "¡Brrr... sigo inmóvil! ❄️")
            advanceTurnBy(_turnDirection.value)
            return
        }

        checkAndTriggerBotTurn()
    }

    fun advanceTurn() {
        advanceTurnBy(_turnDirection.value)
    }

    // Draw cards utility
    private fun drawCardsForPlayer(playerId: String, count: Int, forcedByJudge: Boolean = false) {
        ensureDrawPileNotEmpty(count)
        val player = _players.value.find { it.id == playerId } ?: return

        // Advantage Feature: "Escudo del Juez". If a player is declared with favor/advantage,
        // their draw matches are cut by 50% as premium shielding!
        val effectiveCount = if (player.hasJudgeFavor && !forcedByJudge) {
            val reduced = count / 2
            if (reduced < 1) 1 else reduced
        } else {
            count
        }

        val cardsToGive = mutableListOf<Card>()
        repeat(effectiveCount) {
            if (drawPile.isNotEmpty()) {
                cardsToGive.add(drawPile.removeAt(0))
            }
        }

        _players.value = _players.value.map { p ->
            if (p.id == playerId) p.copy(hand = p.hand + cardsToGive) else p
        }

        val favorLabel = if (player.hasJudgeFavor && !forcedByJudge) " (Reducido de $count gracias al Favor del Juez 👑)" else ""
        addLog("📥 ${player.name} robó exactas $effectiveCount cartas$favorLabel.", isImportant = true)
    }

    // Core AI loop
    private fun checkAndTriggerBotTurn() {
        val currentIdx = _currentTurnIndex.value
        val list = _players.value
        if (list.isEmpty() || currentIdx !in list.indices) return

        val currentPlayer = list[currentIdx]
        if (currentPlayer.isBot) {
            stopBotTurns()

            _isBotThinking.value = true
            _thinkingPlayerId.value = currentPlayer.id

            botTurnJob = viewModelScope.launch {
                // Thinking delays so the user can easily spectate the action
                delay(1200)

                _isBotThinking.value = false
                _thinkingPlayerId.value = null

                executeSmartBotMove(currentPlayer)
            }
        }
    }

    // Smart Bot Brain & Execution
    private fun executeSmartBotMove(bot: Player) {
        val tc = _topCard.value ?: return
        val currentActiveCol = _activeColor.value

        // 1. Scan playable cards
        val playable = bot.hand.filter { it.isCompatibleWith(tc, currentActiveCol) }

        if (playable.isNotEmpty()) {
            // "mucho más listos" - smart bot decision logic!
            val cardToPlay = selectBestBotCard(playable, bot)
            
            // Execute the action
            executeCardPlay(bot.id, cardToPlay)
        } else {
            // No matches, must draw!
            addLog("🔍 ${bot.name} no tiene jugada válida, roba 1 carta.")
            ensureDrawPileNotEmpty(1)
            val drawn = drawPile.removeAt(0)

            val updatedHand = bot.hand + drawn
            _players.value = _players.value.map { p ->
                if (p.id == bot.id) p.copy(hand = updatedHand) else p
            }

            // High Intelligence: If the drawn card is playable immediately, play it!
            if (drawn.isCompatibleWith(tc, currentActiveCol)) {
                addLog("✨ ¡${bot.name} inmediatamente juega la carta recién robada: ${drawn.label}!")
                executeCardPlay(bot.id, drawn)
            } else {
                // If draw did not match, trigger funny failure or normal pass dialogue
                val grumbleQuotes = listOf(
                    "¡Ninguna sirve! Paso turno... 😤",
                    "Esta carta no me ayuda en nada.",
                    "¡Ay caramba, mi mazo sigue engordando!",
                    "¡Paso!"
                )
                triggerSpeakerBubble(bot.id, grumbleQuotes.random())
                advanceTurn()
            }
        }
    }

    // Smart bot selection algorithm
    private fun selectBestBotCard(playable: List<Card>, bot: Player): Card {
        // Detect if another opponent has a dangerous state (hand size <= 2)
        val allOtherPlayers = _players.value.filter { it.id != bot.id }
        val opponentInDanger = allOtherPlayers.any { it.hand.size <= 2 }

        if (opponentInDanger) {
            // HIGH INTELLIGENCE: Prioritize action cards to disable the threatening opponent!
            // First: Mega damage drawers (+50, +20, +10, etc.)
            val megaHits = playable.filter { 
                it.type == CardType.PLUS_50 || it.type == CardType.PLUS_20 || 
                it.type == CardType.PLUS_10 || it.type == CardType.WILD_PLUS_4 
            }
            if (megaHits.isNotEmpty()) {
                val chosen = megaHits.maxByOrNull { it.value }!!
                triggerSpeakerBubble(bot.id, "¡No dejaré que ganes tan fácil con mi ${chosen.label}! 🧨")
                return chosen
            }

            // Second: Normal draw hits (+5, +2, +1)
            val softHits = playable.filter {
                it.type == CardType.PLUS_5 || it.type == CardType.PLUS_2 || it.type == CardType.PLUS_1
            }
            if (softHits.isNotEmpty()) {
                val chosen = softHits.maxByOrNull { it.value }!!
                triggerSpeakerBubble(bot.id, "¡Prueba mi ${chosen.label}! ¡Roba!")
                return chosen
            }

            // Third: Skips & Double Skips & Reverses to steer the turn away
            val blocks = playable.filter { 
                it.type == CardType.DOUBLE_SKIP || it.type == CardType.SKIP || it.type == CardType.REVERSE 
            }
            if (blocks.isNotEmpty()) {
                val chosen = blocks.maxByOrNull { it.value }!!
                triggerSpeakerBubble(bot.id, "¡Lo siento, toca bloquearte con ${chosen.label}! 🚫")
                return chosen
            }
        }

        // Standard strategic play (No instant warnings):
        // Prefer playing number cards first to keep high points/action cards for defensive reserve.
        val numbers = playable.filter { it.type == CardType.NUMBER }
        if (numbers.isNotEmpty()) {
            // Prefer numbers that match colors we have more of (smart harmony)
            val preferredColor = determineSmartColorForBot(bot)
            val matchedColorNumbers = numbers.filter { it.color == preferredColor }
            if (matchedColorNumbers.isNotEmpty()) {
                return matchedColorNumbers.maxByOrNull { it.number ?: 0 }!!
            }
            return numbers.maxByOrNull { it.number ?: 0 }!!
        }

        // If we only have action/wild cards, play the one with the least critical value,
        // saving the ultra cards like PLUS_50 and Wild Draw 4 as last resources!
        val actions = playable.filter { it.type != CardType.WILD_PLUS_4 && it.type != CardType.PLUS_50 }
        if (actions.isNotEmpty()) {
            return actions.random()
        }

        // Return whatever is left
        return playable.random()
    }

    override fun onCleared() {
        stopBotTurns()
        super.onCleared()
    }
}

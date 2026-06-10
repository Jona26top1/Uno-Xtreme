package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.UnoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GameAppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameAppNavigation(modifier: Modifier = Modifier) {
    val viewModel: UnoViewModel = viewModel()
    val mode by viewModel.gameMode.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {
        when (mode) {
            GameMode.MENU -> MainMenuScreen(viewModel)
            GameMode.CLASSIC_PLAY -> ClassicGameScreen(viewModel)
            GameMode.JUDGE_PLAY -> JudgeGameScreen(viewModel)
            GameMode.RULES -> RulesScreen(viewModel)
        }
    }
}

// ---------------- MENU SCREEN ----------------

@Composable
fun MainMenuScreen(viewModel: UnoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / Title Board with UNO style typography
        Card(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .rotate(-3f)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
            border = BorderStroke(4.dp, Color(0xFFFFD700)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "UNO",
                    color = Color(0xFFFFD700),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp,
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "VENTAJAS + JUEZ ⚖️",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Text(
            text = "¡El clásico juego de cartas llevado al extremo cognitivo!",
            color = Color(0xFFB0BEC5),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        )

        // Menu Buttons
        MenuButton(
            text = "Modo Clásico (4 Jugadores)",
            description = "Tú contra 3 Bots súper listos. ¡Cartas de +10, +50 y Bloqueo Doble!",
            icon = Icons.Default.PlayArrow,
            accentColor = Color(0xFF388E3C),
            testTag = "play_classic_button"
        ) {
            viewModel.setGameMode(GameMode.CLASSIC_PLAY)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Modo Juez (⚖️ Espectador)",
            description = "Sé el juez de Ana contra Carlos. Controla el balance de ventaja e influye con poderes.",
            icon = Icons.Default.Star,
            accentColor = Color(0xFF1976D2),
            testTag = "play_judge_button"
        ) {
            viewModel.setGameMode(GameMode.JUDGE_PLAY)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Reglas del Juego",
            description = "Explora las ventajas abusivas y las cartas exclusivas del juez.",
            icon = Icons.Default.Info,
            accentColor = Color(0xFF7B1FA2),
            testTag = "rules_button"
        ) {
            viewModel.setGameMode(GameMode.RULES)
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Desarrollado en Kotlin & Jetpack Compose",
            color = Color(0xFF78909C),
            fontSize = 11.sp
        )
    }
}

@Composable
fun MenuButton(
    text: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C)),
        border = BorderStroke(1.dp, Color(0xFF37474F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF546E7A),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


// ---------------- RULES SCREEN ----------------

@Composable
fun RulesScreen(viewModel: UnoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Back Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setGameMode(GameMode.MENU) },
                modifier = Modifier
                    .testTag("back_to_menu_button")
                    .background(Color(0xFF1C2833), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Reglas y Ventajas",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RuleSectionTitle("🔥 Cartas con Super Ventajas")
            }
            item {
                RuleItem(
                    title = "Cartas de Robo Masivo (+10, +20 y +50)",
                    desc = "Si te juegan estas cartas, el oponente debe robar la cantidad EXACTA indicada (+10, +20, ¡o la increíble de +50!). Cuando se juegan, se salta el turno de quien recibe las cartas.",
                    colorAccent = Color(0xFFD32F2F)
                )
            }
            item {
                RuleItem(
                    title = "Bloqueo Doble (🚫🚫)",
                    desc = "A diferencia del bloqueo común, el Bloqueo Doble salta la participación de los DOS siguientes jugadores de forma inmediata. ¡Perfecto para ganar dominio absoluto de la mesa!",
                    colorAccent = Color(0xFFFF9800)
                )
            }
            item {
                RuleSectionTitle("⚖️ El Rol Supremo de Juez")
            }
            item {
                RuleItem(
                    title = "Influir en la Arena",
                    desc = "En el Modo Juez, observas un duelo 1v1 automático entre Carlos y Ana. Tú sostienes cartas de Juez e influyes en tiempo real. ¡Puedes regalarles 10 cartas, limpiarles la mano, congelarlos, o cambiarles las barajas!",
                    colorAccent = Color(0xFF1976D2)
                )
            }
            item {
                RuleItem(
                    title = "Declarar Ventaja / Favor del Juez 👑",
                    desc = "Puedes presionar el boton para otorgar favor del Juez a un bot. El bot favorecido obtiene un escudo divino: ¡Sufre un 50% de descuento en efectos de cartas robar +N recibidas! (Por ejemplo, un +50 se convierte en +25).",
                    colorAccent = Color(0xFFFFD700)
                )
            }
            item {
                RuleSectionTitle("🧠 Inteligencia Artificial Mejorada")
            }
            item {
                RuleItem(
                    title = "Bots con Pensamiento Táctico",
                    desc = "Los bots analizan cuántas cartas te quedan. Si estás por ganar, te atacarán con todo su arsenal de bloqueos y cartas de robar exactas. El bot del medio (Ana) funciona de forma fluida y reacciona de manera proactiva.",
                    colorAccent = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun RuleSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFFFFD700),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun RuleItem(title: String, desc: String, colorAccent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C21)),
        border = BorderStroke(1.dp, colorAccent.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colorAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                color = Color(0xFFCFD8DC),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}


// ---------------- CLASSIC PLAY SCREEN ----------------

@Composable
fun ClassicGameScreen(viewModel: UnoViewModel) {
    val players by viewModel.players.collectAsState()
    val topCard by viewModel.topCard.collectAsState()
    val activeColor by viewModel.activeColor.collectAsState()
    val currentTurnIndex by viewModel.currentTurnIndex.collectAsState()
    val direction by viewModel.turnDirection.collectAsState()
    val isBotThinking by viewModel.isBotThinking.collectAsState()
    val thinkingPlayerId by viewModel.thinkingPlayerId.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val showWildDialog by viewModel.showWildDialog.collectAsState()
    val notification by viewModel.notification.collectAsState()

    var showWildSelectionForCard by remember { mutableStateOf<Card?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.setGameMode(GameMode.MENU) },
                    modifier = Modifier.background(Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Menú Principal",
                        tint = Color.White
                    )
                }

                // Turn and active direction badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x66000000)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (direction == 1) Icons.Default.ArrowForward else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (direction == -1) 180f else 0f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (direction == 1) "Sentido Horario" else "Sentido Antihorario",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Restart
                IconButton(
                    onClick = { viewModel.setGameMode(GameMode.CLASSIC_PLAY) },
                    modifier = Modifier.background(Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reiniciar",
                        tint = Color.White
                    )
                }
            }

            // 1. Bots Area (Three bots: Carlos [0], Ana [1], Pedro [2])
            val bots = players.filter { it.isBot }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bots.forEach { bot ->
                    val isHisTurn = players.getOrNull(currentTurnIndex)?.id == bot.id
                    val isCurrentlyThinking = isBotThinking && thinkingPlayerId == bot.id
                    BotStatusCard(
                        bot = bot,
                        isCurrentTurn = isHisTurn,
                        isThinking = isCurrentlyThinking
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Play Board Arena
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1B4F72), Color(0xFF0F1E24)),
                            radius = 400f
                        )
                    )
                    .border(2.dp, Color(0xFF2874A6), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Draw Deck Graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Only draw if user turn
                                if (players.getOrNull(currentTurnIndex)?.id == "user") {
                                    viewModel.userDrawCard()
                                }
                            }
                    ) {
                        Card(
                            modifier = Modifier
                                .size(90.dp, 130.dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFBF360C)),
                            border = BorderStroke(2.dp, Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw stack card details
                                Card(
                                    modifier = Modifier
                                        .size(70.dp, 108.dp)
                                        .rotate(-5f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD84315)),
                                    border = BorderStroke(1.dp, Color(0xFFFFAB91)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "UNO",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Robar",
                            color = Color(0xFFCFD8DC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("draw_card_button")
                        )
                    }

                    // Discard Pile Graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        topCard?.let { card ->
                            GameCardUI(
                                card = card,
                                activeColor = activeColor,
                                modifier = Modifier
                                    .size(90.dp, 130.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                                isPlayableStyle = false
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Color: ${activeColor.displayName}",
                            color = when (activeColor) {
                                CardColor.RED -> Color(0xFFEF5350)
                                CardColor.GREEN -> Color(0xFF66BB6A)
                                CardColor.BLUE -> Color(0xFF42A5F5)
                                CardColor.YELLOW -> Color(0xFFFFEE58)
                                else -> Color.White
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Status Logs Monitor (Scrollable updates of gameplay)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f),
                colors = CardDefaults.cardColors(containerColor = Color(0x990F1416)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "📋 Registro de Jugadas:",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[${log.timestamp}] ",
                                    color = Color(0xFF546E7A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = log.message,
                                    color = if (log.isImportant) Color(0xFFFFD700) else Color(0xFFECEFF1),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. User Hand Area
            val user = players.find { it.id == "user" }
            val isMyTurn = players.getOrNull(currentTurnIndex)?.id == "user"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D21)),
                border = BorderStroke(1.dp, if (isMyTurn) Color(0xFFFFD700) else Color(0xFF263238)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMyTurn) "👉 ¡TU TURNO! Juega una carta:" else "Esperando turno...",
                            color = if (isMyTurn) Color(0xFFFFD700) else Color(0xFF90A4AE),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${user?.hand?.size ?: 0} cartas",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (user != null && user.hand.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(user.hand, key = { it.id }) { card ->
                                val isPlayable = card.isCompatibleWith(topCard!!, activeColor)
                                val modifier = Modifier
                                    .size(80.dp, 115.dp)
                                    .testTag("user_card_item_${card.id}")
                                    .clickable(enabled = isMyTurn) {
                                        if (isPlayable) {
                                            if (card.color == CardColor.WILD) {
                                                showWildSelectionForCard = card
                                            } else {
                                                viewModel.userPlayCard(card)
                                            }
                                        }
                                    }

                                GameCardUI(
                                    card = card,
                                    activeColor = activeColor,
                                    modifier = modifier,
                                    isPlayableStyle = isPlayable && isMyTurn
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin cartas en la mano.",
                                color = Color(0xFF78909C),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Animated overlay alert notification
        AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            notification?.let { text ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xE6263238)),
                    border = BorderStroke(2.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                    )
                }
            }
        }

        // Wild card color picker Dialog
        showWildSelectionForCard?.let { card ->
            Dialog(onDismissRequest = { showWildSelectionForCard = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E282D)),
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selecciona Color Comodín",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val colorsList = listOf(
                            CardColor.RED to Color(0xFFEF5350),
                            CardColor.GREEN to Color(0xFF66BB6A),
                            CardColor.BLUE to Color(0xFF42A5F5),
                            CardColor.YELLOW to Color(0xFFFFEE58)
                        )

                        colorsList.chunked(2).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowColors.forEach { (colorKey, colorVal) ->
                                    Button(
                                        onClick = {
                                            viewModel.userPlayCard(card, colorKey)
                                            showWildSelectionForCard = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colorVal),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp)
                                            .height(55.dp)
                                    ) {
                                        Text(
                                            text = colorKey.displayName,
                                            color = if (colorKey == CardColor.YELLOW) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Winner Dialog
        winner?.let { winningPlayer ->
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2833)),
                    border = BorderStroke(3.dp, Color(0xFFFFD700))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 ¡TENEMOS UN GANADOR! 🎉",
                            color = Color(0xFFFFD700),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = winningPlayer.avatarEmoji,
                            fontSize = 72.sp,
                            modifier = Modifier.padding(8.dp)
                        )

                        Text(
                            text = winningPlayer.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (winningPlayer.id == "user") "¡Has derrotado a las inteligencias artificiales! Escribiste historia hoy."
                                   else "La IA ${winningPlayer.name} ha demostrado un intelecto superior. ¡Sigue practicando!",
                            color = Color(0xFFCFD8DC),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.setGameMode(GameMode.MENU) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Volver al Menú Principal",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- JUDGE GAME SCREEN (Modo Juez ⚖️) ----------------

@Composable
fun JudgeGameScreen(viewModel: UnoViewModel) {
    val players by viewModel.players.collectAsState()
    val topCard by viewModel.topCard.collectAsState()
    val activeColor by viewModel.activeColor.collectAsState()
    val currentTurnIndex by viewModel.currentTurnIndex.collectAsState()
    val isBotThinking by viewModel.isBotThinking.collectAsState()
    val thinkingPlayerId by viewModel.thinkingPlayerId.collectAsState()
    val judgeHand by viewModel.judgeHand.collectAsState()
    val selectedJudgeWinner by viewModel.selectedJudgeWinner.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val winner by viewModel.winner.collectAsState()

    var activeCastingJudgeCard by remember { mutableStateOf<JudgeCard?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.setGameMode(GameMode.MENU) },
                    modifier = Modifier.background(Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Menú",
                        tint = Color.White
                    )
                }

                Text(
                    text = "⚖️ TRIBUNAL DEL JUEZ ⚖️",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                IconButton(
                    onClick = { viewModel.setGameMode(GameMode.JUDGE_PLAY) },
                    modifier = Modifier.background(Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reiniciar",
                        tint = Color.White
                    )
                }
            }

            // 1. Core Arena of bots dueling (1v1: Carlos [0] vs Ana [1])
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                players.forEachIndexed { idx, bot ->
                    val isHisTurn = currentTurnIndex == idx
                    val isCurrentlyThinking = isBotThinking && thinkingPlayerId == bot.id
                    val colorAccent = if (bot.id == "bot_0") Color(0xFFEF6C00) else Color(0xFF3F51B5)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(
                                width = 2.dp,
                                color = if (isHisTurn) Color(0xFFFFD700) else colorAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B1F)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            // Header of bot state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = bot.avatarEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = bot.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Row {
                                    if (bot.hasJudgeFavor) {
                                        Text(
                                            text = "👑🛡️",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                    if (bot.freezeTurns > 0) {
                                        Text(
                                            text = "❄️",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Thinking indicator
                            if (isCurrentlyThinking) {
                                Text(
                                    text = "Pensando jugada...",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            } else if (isHisTurn) {
                                Text(
                                    text = "👉 TURNO ACTIVO",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "Esperando...",
                                    color = Color(0xFF90A4AE),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            // Small speech bubble
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(vertical = 2.dp)
                            ) {
                                bot.speechBubble?.let { text ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = colorAccent.copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, colorAccent.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = text,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                lineHeight = 11.sp,
                                                textAlign = TextAlign.Center,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Show bot's cards (Omnipotent observer view!)
                            Text(
                                text = "Cartas (${bot.hand.size}):",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            LazyVerticalGridSimple(
                                items = bot.hand,
                                modifier = Modifier.weight(1f)
                            ) { card ->
                                Card(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(38.dp, 54.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (card.color) {
                                            CardColor.RED -> Color(0xFFD32F2F)
                                            CardColor.GREEN -> Color(0xFF388E3C)
                                            CardColor.BLUE -> Color(0xFF1976D2)
                                            CardColor.YELLOW -> Color(0xFFFBC02D)
                                            else -> Color(0xFF263238)
                                        }
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.label.take(3),
                                            color = if (card.color == CardColor.YELLOW) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Play Board (Discard & Active color)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11252C)),
                border = BorderStroke(1.dp, Color(0xFF233B42)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "POZO DE DESCARTE",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        topCard?.let { card ->
                            Text(
                                text = card.label,
                                color = when (card.color) {
                                    CardColor.RED -> Color(0xFFEF5350)
                                    CardColor.GREEN -> Color(0xFF66BB6A)
                                    CardColor.BLUE -> Color(0xFF42A5F5)
                                    CardColor.YELLOW -> Color(0xFFFFEE58)
                                    else -> Color.White
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.White, CircleShape)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "COLOR ACTIVO",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeColor.displayName.uppercase(),
                            color = when (activeColor) {
                                CardColor.RED -> Color(0xFFEF5350)
                                CardColor.GREEN -> Color(0xFF66BB6A)
                                CardColor.BLUE -> Color(0xFF42A5F5)
                                CardColor.YELLOW -> Color(0xFFFFEE58)
                                else -> Color.White
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. The Judge Throne Powers (Judge Actions & Advantage designation)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                border = BorderStroke(1.dp, Color(0xFF2E2E3F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Title
                    Text(
                        text = "⚖️ ACCIONES JUDICIALES (TÚ DIRIGES):",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Advantage Declaring Row
                    Text(
                        text = "Designar ventaja (Atribuye 👑 Favor del Juez reduciendo cobros de mazo al 50%):",
                        color = Color(0xFFB0BEC5),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val carlosId = players.getOrNull(0)?.id ?: ""
                        val anaId = players.getOrNull(1)?.id ?: ""

                        Button(
                            onClick = { viewModel.declareJudgeWinner(carlosId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedJudgeWinner == carlosId) Color(0xFFE65100) else Color(0xFF2E3236)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("declare_advantage_carlos")
                        ) {
                            Text(
                                text = "🏆 Ventaja Carlos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { viewModel.declareJudgeWinner(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedJudgeWinner == null) Color(0xFF455A64) else Color(0xFF2E3236)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(38.dp)
                                .testTag("neutral_advantage")
                        ) {
                            Text(
                                text = "Equilibrar",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { viewModel.declareJudgeWinner(anaId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedJudgeWinner == anaId) Color(0xFF1A237E) else Color(0xFF2E3236)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("declare_advantage_ana")
                        ) {
                            Text(
                                text = "🏆 Ventaja Ana",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Judge cards hand
                    Text(
                        text = "Cartas Especiales de la Corte (Haz clic en una para influir):",
                        color = Color(0xFFB0BEC5),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(judgeHand, key = { it.id }) { jCard ->
                            Card(
                                modifier = Modifier
                                    .size(100.dp, 60.dp)
                                    .testTag("judge_card_item_${jCard.type}")
                                    .clickable { activeCastingJudgeCard = jCard },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF282836)),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${jCard.emoji} ${jCard.name}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = jCard.description.take(24) + "...",
                                        color = Color(0xFF90A4AE),
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Compact Logs for Spectating (bottom)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                colors = CardDefaults.cardColors(containerColor = Color(0x990F1416)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "📋 Registro del Tribunal:",
                        color = Color(0xFF90A4AE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "[${log.timestamp}] ",
                                    color = Color(0xFF546E7A),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = log.message,
                                    color = if (log.isImportant) Color(0xFFFFD700) else Color(0xFFECEFF1),
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Casting Target Modals
        activeCastingJudgeCard?.let { jCard ->
            val bots = players.filter { it.isBot }
            Dialog(onDismissRequest = { activeCastingJudgeCard = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222230)),
                    border = BorderStroke(2.dp, Color(0xFFFFD700))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${jCard.emoji} Lanzar ${jCard.name}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = jCard.description,
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Resolve Targets
                        if (jCard.type == JudgeCardType.EARTHQUAKE_SWAP || jCard.type == JudgeCardType.COLOR_TERREMOTO) {
                            // Targetless global action
                            Button(
                                onClick = {
                                    viewModel.playJudgeCard(jCard, null)
                                    activeCastingJudgeCard = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Desatar Evento Supremo",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Target-based (Carlos or Ana)
                            bots.forEach { bot ->
                                Button(
                                    onClick = {
                                        viewModel.playJudgeCard(jCard, bot.id)
                                        activeCastingJudgeCard = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (bot.id == "bot_0") Color(0xFFEF6C00) else Color(0xFF3F51B5)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(45.dp)
                                ) {
                                    Text(
                                        text = "Objetivo: ${bot.avatarEmoji} ${bot.name}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { activeCastingJudgeCard = null }) {
                            Text(text = "Cancelar", color = Color.White)
                        }
                    }
                }
            }
        }

        // Winner Dialog
        winner?.let { winningPlayer ->
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2833)),
                    border = BorderStroke(3.dp, Color(0xFFFFD700))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚖️ SENTENCIA FINAL JURÍDICA ⚖️",
                            color = Color(0xFFFFD700),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = winningPlayer.avatarEmoji,
                            fontSize = 72.sp,
                            modifier = Modifier.padding(8.dp)
                        )

                        Text(
                            text = "${winningPlayer.name} Gana la Partida",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Como Juez has supervisado y sentenciado un duelo de cartas épico. El tribunal ha cerrado sus puertas.",
                            color = Color(0xFFCFD8DC),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.setGameMode(GameMode.MENU) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Volver al Menú Principal",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// ---------------- HELPER EXPERT COMPONENTS ----------------

@Composable
fun BotStatusCard(
    bot: Player,
    isCurrentTurn: Boolean,
    isThinking: Boolean
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .border(
                width = 2.dp,
                color = if (isCurrentTurn) Color(0xFFFFD700) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152228)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Row
            Box {
                Text(
                    text = bot.avatarEmoji,
                    fontSize = 28.sp
                )
                // Crown if favored
                if (bot.hasJudgeFavor) {
                    Text(
                        text = "👑",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                    )
                }
                // Ice if frozen
                if (bot.freezeTurns > 0) {
                    Text(
                        text = "❄️",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bot.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Dynamic State
            if (isThinking) {
                Text(
                    text = "Pensando...",
                    color = Color(0xFFFFD700),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "${bot.hand.size} cartas",
                    color = Color(0xFF90A4AE),
                    fontSize = 10.sp
                )
            }

            // Compact dialogue bubble
            bot.speechBubble?.let { quote ->
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = quote,
                        color = Color.White,
                        fontSize = 7.sp,
                        lineHeight = 9.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameCardUI(
    card: Card,
    activeColor: CardColor,
    modifier: Modifier = Modifier,
    isPlayableStyle: Boolean = false
) {
    val containerCol = when (card.color) {
        CardColor.RED -> Color(0xFFD32F2F)
        CardColor.GREEN -> Color(0xFF388E3C)
        CardColor.BLUE -> Color(0xFF1976D2)
        CardColor.YELLOW -> Color(0xFFFBC02D)
        else -> Color(0xFF212121) // Wild / Black card
    }

    val contentCol = if (card.color == CardColor.YELLOW) Color.Black else Color.White

    // Glowing active border if playable
    val cardBorder = if (isPlayableStyle) {
        BorderStroke(3.dp, Color(0xFFFFD700))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    }

    Card(
        modifier = modifier
            .scale(if (isPlayableStyle) 1.05f else 1f)
            .shadow(if (isPlayableStyle) 10.dp else 4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = containerCol),
        border = cardBorder,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Top Left Mini Label
            Text(
                text = when (card.type) {
                    CardType.WILD -> "🌈"
                    CardType.WILD_PLUS_4 -> "+4"
                    else -> card.label.substringBefore(" ")
                },
                color = contentCol,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Center Primary Icon / Number
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (card.type) {
                        CardType.NUMBER -> card.number?.toString() ?: "0"
                        CardType.PLUS_1 -> "+1"
                        CardType.PLUS_2 -> "+2"
                        CardType.PLUS_5 -> "+5"
                        CardType.PLUS_10 -> "+10"
                        CardType.PLUS_20 -> "+20"
                        CardType.PLUS_50 -> "+50"
                        CardType.SKIP -> "🚫"
                        CardType.DOUBLE_SKIP -> "🚫🚫"
                        CardType.REVERSE -> "🔄"
                        CardType.WILD -> "🌈"
                        CardType.WILD_PLUS_4 -> "🌈+4"
                    },
                    color = contentCol,
                    fontSize = when (card.type) {
                        CardType.DOUBLE_SKIP -> 14.sp
                        CardType.WILD_PLUS_4 -> 14.sp
                        else -> 22.sp
                    },
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                if (card.type == CardType.DOUBLE_SKIP) {
                    Text(
                        text = "DOBLE",
                        color = contentCol,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Right Mini Label
            Text(
                text = when (card.type) {
                    CardType.WILD -> "🌈"
                    CardType.WILD_PLUS_4 -> "+4"
                    else -> card.label.substringBefore(" ")
                },
                color = contentCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .rotate(180f)
            )
        }
    }
}

// Custom simple vertical grid helper that works beautifully for compact items
@Composable
fun LazyVerticalGridSimple(
    items: List<Card>,
    modifier: Modifier = Modifier,
    content: @Composable (Card) -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val chunks = items.chunked(3)
            items(chunks) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            content(item)
                        }
                    }
                    // Fill empty spots for clean layout
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

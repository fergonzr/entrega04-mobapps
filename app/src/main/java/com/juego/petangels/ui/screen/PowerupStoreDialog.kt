package com.juego.petangels.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juego.petangels.R
import com.juego.petangels.debug.RICH_DEBUG_STATE
import com.juego.petangels.domain.GameState
import com.juego.petangels.domain.Powerup
import com.juego.petangels.domain.PowerupType
import com.juego.petangels.ui.theme.AngelOrphanageTheme
import kotlinx.coroutines.delay

private val DialogBackground = Color(0xFFF3E7D3)
private val DialogBorder = Color(0xFFB08D78)
private val CardBackground = Color(0xFFD9B89A)
private val SelectedCardBackground = Color(0xFFE9CF89)
private val CardBorder = Color(0xFF7A4F33)
private val ConfirmGreen = Color(0xFF4CAF50)
private val ResetBrown = Color(0xFF8D6E63)
private val DisabledGray = Color(0xFFBCAAA4)
private val MutedText = Color(0xFF5D4037)
private val TitleText = Color(0xFF3E2723)
private val PointsGold = Color(0xFF8D4B20)

private val POWERUP_ICONS = mapOf(
    PowerupType.COMFORT to "\uD83D\uDC95",
    PowerupType.VISIBILITY to "\uD83D\uDCE2",
    PowerupType.FOOD_INCREMENT to "\uD83C\uDF56",
    PowerupType.WATER_INCREMENT to "\uD83D\uDCA7"
)

private fun PowerupType.labelRes(): Int = when (this) {
    PowerupType.COMFORT -> R.string.comfort_label
    PowerupType.VISIBILITY -> R.string.adoption_marketing_label
    PowerupType.FOOD_INCREMENT -> R.string.food_increment_label
    PowerupType.WATER_INCREMENT -> R.string.water_increment_label
}

private fun PowerupType.descriptionRes(): Int = when (this) {
    PowerupType.COMFORT -> R.string.comfort_desc
    PowerupType.VISIBILITY -> R.string.adoption_marketing_desc
    PowerupType.FOOD_INCREMENT -> R.string.food_increment_desc
    PowerupType.WATER_INCREMENT -> R.string.water_increment_desc
}

@Composable
fun PowerupStoreDialog(
    gameState: GameState,
    onConfirm: (GameState) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingPurchases by remember { mutableStateOf<Map<PowerupType, Int>>(emptyMap()) }

    val currentLevels = PowerupType.entries.associateWith { type ->
        gameState.powerups.find { it.type == type }?.level ?: 0
    }

    val totalPendingCost = pendingPurchases.entries.sumOf { (type, levels) ->
        type.costPerlevel * levels
    }

    val availablePoints = gameState.score

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .padding(top = 52.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DialogBackground)
                .border(1.dp, DialogBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tienda de Power Ups",
                color = TitleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Puntos Disponibles: ",
                    color = MutedText,
                    fontSize = 10.sp
                )
                Text(
                    text = "$availablePoints",
                    color = PointsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = DialogBorder)

            val powerupTypes = PowerupType.entries.sortedBy { it.order }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                powerupTypes.forEach { type ->
                    val currentLevel = currentLevels[type] ?: 0
                    val pendingLevels = pendingPurchases[type] ?: 0
                    val newLevel = currentLevel + pendingLevels
                    val reachedMaxLevel = newLevel >= type.maxLevel
                    val hasEnoughCoins = (totalPendingCost + type.costPerlevel) <= availablePoints
                    val canBuyMore = !reachedMaxLevel && hasEnoughCoins
                    val isSelected = pendingLevels > 0

                    PowerupCard(
                        type = type,
                        currentLevel = currentLevel,
                        pendingLevels = pendingLevels,
                        maxLevel = type.maxLevel,
                        costPerLevel = type.costPerlevel,
                        isSelected = isSelected,
                        canBuyMore = canBuyMore,
                        insufficientCoins = !reachedMaxLevel && !hasEnoughCoins,
                        onTap = {
                            if (canBuyMore) {
                                pendingPurchases = pendingPurchases.toMutableMap().apply {
                                    this[type] = (this[type] ?: 0) + 1
                                }
                            } else if (reachedMaxLevel) {
                                Toast.makeText(
                                    context,
                                    "Este powerup ya está al máximo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Monedas insuficientes",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (totalPendingCost > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Costo total:",
                        color = MutedText,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$totalPendingCost puntos",
                        color = PointsGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { pendingPurchases = emptyMap() },
                    enabled = pendingPurchases.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResetBrown,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledGray,
                        disabledContentColor = Color(0xFF6D4C41)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                ) {
                    Text("Limpiar", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        var newState = gameState
                        pendingPurchases.forEach { (type, levels) ->
                            newState = newState.buy_powerup(Powerup(type, levels))
                        }
                        onConfirm(newState)
                    },
                    enabled = pendingPurchases.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConfirmGreen,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledGray,
                        disabledContentColor = Color(0xFF6D4C41)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PowerupCard(
    type: PowerupType,
    currentLevel: Int,
    pendingLevels: Int,
    maxLevel: Int,
    costPerLevel: Int,
    isSelected: Boolean,
    canBuyMore: Boolean,
    insufficientCoins: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground = if (isSelected) SelectedCardBackground else CardBackground
    val borderColor = if (isSelected) CardBorder else Color(0xFFB08D78)
    val newLevel = (currentLevel + pendingLevels).coerceAtMost(maxLevel)
    var showPurchasedText by remember { mutableStateOf(false) }
    var showInsufficientText by remember { mutableStateOf(false) }
    var bounce by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (bounce) 1.07f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "powerupBuyScale"
    )

    LaunchedEffect(pendingLevels) {
        if (pendingLevels > 0) {
            bounce = true
            showPurchasedText = true
            delay(160)
            bounce = false
            delay(640)
            showPurchasedText = false
        }
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable {
                if (insufficientCoins) {
                    bounce = true
                    showInsufficientText = true
                }
                onTap()
            }
            .height(112.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = POWERUP_ICONS[type] ?: "\u2728",
            fontSize = 14.sp
        )
        Text(
            text = stringResource(type.labelRes()),
            color = TitleText,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxLevel) {
                val dotColor = when {
                    i <= currentLevel -> Color(0xFF4CAF50)
                    i <= newLevel -> Color(0xFF4FC3F7)
                    else -> Color(0xFFBCAAA4)
                }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
            }
            Text(
                text = newLevel.toString(),
                color = TitleText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(type.descriptionRes()),
            color = MutedText,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        LaunchedEffect(showInsufficientText) {
            if (showInsufficientText) {
                delay(160)
                bounce = false
                delay(840)
                showInsufficientText = false
            }
        }
        AnimatedVisibility(
            visible = showPurchasedText || showInsufficientText,
            enter = fadeIn(animationSpec = tween(120)),
            exit = fadeOut(animationSpec = tween(160))
        ) {
            Text(
                text = if (showInsufficientText) "Monedas insuficientes" else "Power Up comprado",
                color = if (showInsufficientText) Color(0xFFE53935) else ConfirmGreen,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        AnimatedVisibility(visible = !showPurchasedText && !showInsufficientText) {
            Text(
                text = "$costPerLevel \uD83E\uDE99/nivel",
                color = MutedText,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(
    name = "Powerup Store Dialog",
    showBackground = true,
    widthDp = 400,
    heightDp = 500
)
@Composable
fun PowerupStoreDialogPreview() {
    AngelOrphanageTheme {
        PowerupStoreDialog(
            gameState = RICH_DEBUG_STATE,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

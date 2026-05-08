package com.example.angelorphanage.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.example.angelorphanage.R
import com.example.angelorphanage.debug.RICH_DEBUG_STATE
import com.example.angelorphanage.domain.GameState
import com.example.angelorphanage.domain.Powerup
import com.example.angelorphanage.domain.PowerupType
import com.example.angelorphanage.ui.theme.AngelOrphanageTheme

// ─── Colors ────────────────────────────────────────────────────────────

private val DialogBackground = Color(0xFF1A1A2E)
private val CardBackground = Color(0xFF16213E)
private val SelectedCardBackground = Color(0xFF3D2E7A)
private val AccentPurple = Color(0xFF7E57C2)
private val ConfirmGreen = Color(0xFF4CAF50)
private val ResetRed = Color(0xFFE57373)
private val PointsGold = Color(0xFFFFD700)
private val PreviewBlue = Color(0xFF4FC3F7)
private val DisabledGray = Color(0xFF424242)
private val MutedText = Color(0xFFBBBBCC)

// ─── Emoji icons per powerup type ──────────────────────────────────────

private val POWERUP_ICONS = mapOf(
    PowerupType.COMFORT to "\uD83D\uDC95",         // 💕
    PowerupType.VISIBILITY to "\uD83D\uDCE2",      // 📢
    PowerupType.FOOD_INCREMENT to "\uD83C\uDF56",   // 🍖
    PowerupType.WATER_INCREMENT to "\uD83D\uDCA7",  // 💧
)

// ─── String resource helpers ───────────────────────────────────────────
// PowerupType stores @StringRes parameters but doesn't expose them as
// properties, so we map them explicitly.

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

// ─── Dialog ────────────────────────────────────────────────────────────

/**
 * A modal dialog for purchasing powerups with accumulated points.
 *
 * UX flow:
 * 1. Player taps a powerup card to queue one additional level for purchase.
 *    Cards with pending purchases are highlighted in a different color.
 * 2. "Confirmar" applies all queued purchases and closes the dialog.
 * 3. "Limpiar" resets the selection without buying anything.
 * 4. "Cancelar" dismisses the dialog without buying anything.
 *
 * Validation:
 * - A powerup cannot exceed its [PowerupType.maxLevel].
 * - Total cost of queued purchases cannot exceed the player's current score.
 */
@Composable
fun PowerupStoreDialog(
    gameState: GameState,
    onConfirm: (GameState) -> Unit,
    onDismiss: () -> Unit
) {
    // How many additional levels the player wants to buy per type
    var pendingPurchases by remember {
        mutableStateOf<Map<PowerupType, Int>>(emptyMap())
    }

    // Current level for each powerup type (0 if not yet purchased)
    val currentLevels = PowerupType.entries.associateWith { type ->
        gameState.powerups.find { it.type == type }?.level ?: 0
    }

    // Total cost of all pending purchases
    val totalPendingCost = pendingPurchases.entries.sumOf { (type, levels) ->
        type.costPerlevel * levels
    }

    val availablePoints = gameState.score

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DialogBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header ──────────────────────────────────────────────
            Text(
                text = "\uD83D\uDED2 Tienda de Powerups",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Available points display
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uD83E\uDE99 Puntos disponibles: ",
                    color = MutedText,
                    fontSize = 13.sp
                )
                Text(
                    text = "$availablePoints",
                    color = PointsGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = Color(0xFF333355))

            val powerupTypes = PowerupType.entries.sortedBy { it.order }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                powerupTypes.forEach { type ->
                    val currentLevel = currentLevels[type] ?: 0
                    val pendingLevels = pendingPurchases[type] ?: 0
                    val newLevel = currentLevel + pendingLevels
                    val canBuyMore = newLevel < type.maxLevel
                            && (totalPendingCost + type.costPerlevel) <= availablePoints
                    val isSelected = pendingLevels > 0

                    PowerupCard(
                        type = type,
                        currentLevel = currentLevel,
                        pendingLevels = pendingLevels,
                        maxLevel = type.maxLevel,
                        costPerLevel = type.costPerlevel,
                        isSelected = isSelected,
                        canBuyMore = canBuyMore,
                        onTap = {
                            if (canBuyMore) {
                                pendingPurchases = pendingPurchases.toMutableMap().apply {
                                    this[type] = (this[type] ?: 0) + 1
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            // ── Total cost ──────────────────────────────────────────
            if (totalPendingCost > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Costo total:",
                        color = MutedText,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$totalPendingCost puntos",
                        color = PointsGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Action buttons ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reset button
                Button(
                    onClick = { pendingPurchases = emptyMap() },
                    enabled = pendingPurchases.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResetRed,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledGray,
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar", fontWeight = FontWeight.Bold)
                }

                // Confirm button
                Button(
                    onClick = {
                        var newState = gameState
                        pendingPurchases.forEach { (type, levels) ->
                            repeat(levels) {
                                newState = newState.buy_powerup(Powerup(type))
                            }
                        }
                        onConfirm(newState)
                    },
                    enabled = pendingPurchases.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConfirmGreen,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledGray,
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold)
                }
            }

            // Cancel / close button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF757575),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Powerup card ──────────────────────────────────────────────────────

/**
 * A single powerup entry inside the store dialog.
 * Tapping the card queues one additional level for purchase
 * (if [canBuyMore] is true).
 */
@Composable
private fun PowerupCard(
    type: PowerupType,
    currentLevel: Int,
    pendingLevels: Int,
    maxLevel: Int,
    costPerLevel: Int,
    isSelected: Boolean,
    canBuyMore: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val newLevel = currentLevel + pendingLevels
    val cardBackground = if (isSelected) SelectedCardBackground else CardBackground
    val borderColor = if (isSelected) AccentPurple else Color(0xFF333366)

    Column(
        modifier = modifier
            .widthIn(min = 500.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = canBuyMore, onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        // Top row: emoji + name + level dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = POWERUP_ICONS[type] ?: "\u2728",
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(type.labelRes()),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Level dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..maxLevel) {
                    val dotColor = when {
                        i <= currentLevel -> ConfirmGreen
                        i <= newLevel -> PreviewBlue
                        else -> Color(0x40FFFFFF)
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (pendingLevels > 0) "$currentLevel→$newLevel" else "$currentLevel",
                    color = if (pendingLevels > 0) PreviewBlue else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Description
        Text(
            text = stringResource(type.descriptionRes()),
            color = MutedText,
            fontSize = 10.sp,
            lineHeight = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Cost per level (right-aligned)
        Text(
            text = "$costPerLevel \uD83E\uDE99/nivel",
            color = PointsGold,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ─── Preview ──────────────────────────────────────────────────────────

@Preview(
    name = "Powerup Store Dialog",
    showBackground = true,
    widthDp = 400,
    heightDp = 500
)
@Composable
fun PowerupStoreDialogPreview() {
    // RICH_DEBUG_STATE has score=420 and one COMFORT powerup at level 1,
    // which is ideal for previewing: some owned, some affordable.
    AngelOrphanageTheme {
        PowerupStoreDialog(
            gameState = RICH_DEBUG_STATE,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
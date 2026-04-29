package com.example.angelorphanage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelorphanage.domain.GameState
import com.example.angelorphanage.domain.ResourceType
import com.example.angelorphanage.domain.ScorerInstance

@Composable
fun GameScreen() {
    // Game state management
    var gameState by remember { mutableStateOf(GameState()) }
    var allocationMap by remember { mutableStateOf<List<Map<ResourceType, Int>>>(List(gameState.scorers.size) { emptyMap() }) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GameTopBar(score = gameState.score, elapsedTurns = gameState.elapsedTurns)
        },
        bottomBar = {
            GameBottomBar(
                onRunTurn = {
                    // Run the game with current allocation
                    gameState = gameState.run(allocationMap)
                    // Reset allocation map for next turn
                    allocationMap = List(gameState.scorers.size) { emptyMap() }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ResourceDisplay(currentResources = gameState.currentResources)
            Spacer(modifier = Modifier.height(16.dp))
            ScorerList(
                scorers = gameState.scorers,
                currentResources = gameState.currentResources,
                allocationMap = allocationMap,
                onUpdateAllocation = { index, newAllocation ->
                    // Update the allocation for specific scorer
                    val newAllocationMap = allocationMap.toMutableList()
                    newAllocationMap[index] = newAllocation
                    allocationMap = newAllocationMap
                }
            )
        }
    }
}

@Composable
fun GameTopBar(score: Int, elapsedTurns: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Turn", fontWeight = FontWeight.Bold)
            Text(text = elapsedTurns.toString())
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Score", fontWeight = FontWeight.Bold)
            Text(text = score.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GameBottomBar(onRunTurn: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onRunTurn,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Run Turn", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResourceDisplay(currentResources: Map<ResourceType, Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ResourceType.entries.forEach { resourceType ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(resourceType.name.replace("_", " "), fontWeight = FontWeight.Bold)
                Text(text = currentResources[resourceType].toString())
            }
        }
    }
}

@Composable
fun ScorerList(
    scorers: List<ScorerInstance>,
    currentResources: Map<ResourceType, Int>,
    allocationMap: List<Map<ResourceType, Int>>,
    onUpdateAllocation: (Int, Map<ResourceType, Int>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight(0.6f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(scorers.filter { !it.givenAway }.withIndex().toList() ) { (index, scorer) ->
            ScorerCard(
                index = index,
                scorer = scorer,
                currentResources = currentResources,
                currentAllocation = allocationMap.getOrElse(index) { emptyMap() },
                onUpdateAllocation = { newAllocation ->
                    onUpdateAllocation(index, newAllocation)
                }
            )
        }
    }
}

@Composable
fun ScorerCard(
    index: Int,
    scorer: ScorerInstance,
    currentResources: Map<ResourceType, Int>,
    currentAllocation: Map<ResourceType, Int>,
    onUpdateAllocation: (Map<ResourceType, Int>) -> Unit
) {
    // Get current allocation values
    val currentFood = currentAllocation[ResourceType.FOOD] ?: 0
    val currentWater = currentAllocation[ResourceType.WATER] ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(16.dp)
            .clickable { /* Optional expand/collapse behavior */ },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = scorer.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = scorer.type.toString().replace("_", " "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (scorer.givenAway) {
                Text(
                    text = "GIVEN AWAY",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Score: ${scorer.lastGeneratedScore}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Resource meters display
        ResourceType.entries.forEach { resourceType ->
            val meterValue = scorer.meters[resourceType] ?: 0
            val limit = scorer.type.meterLimits[resourceType]
            val (minLimit, maxLimit) = limit ?: Pair(0, 0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${resourceType.name.replace("_", " ")}:")
                Text(
                    text = "$meterValue / $maxLimit",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (meterValue >= maxLimit) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((meterValue.toFloat() / maxLimit.coerceAtLeast(1)).coerceIn(0.0f, 1.0f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2196F3))
                )
            }
        }

        // Allocation controls - directly update allocation map
        if (!scorer.givenAway) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text("Food:")
                    Row {
                        Button(
                            onClick = {
                                val newFood = (currentFood - 1).coerceAtLeast(0)
                                onUpdateAllocation(mapOf(
                                    ResourceType.FOOD to newFood,
                                    ResourceType.WATER to currentWater
                                ))
                            },
                            modifier = Modifier.width(32.dp)
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = currentFood.toString())
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateAllocation(mapOf(
                                    ResourceType.FOOD to currentFood + 1,
                                    ResourceType.WATER to currentWater
                                ))
                            },
                            modifier = Modifier.width(32.dp)
                        ) {
                            Text("+")
                        }
                    }
                }
                Column {
                    Text("Water:")
                    Row {
                        Button(
                            onClick = {
                                val newWater = (currentWater - 1).coerceAtLeast(0)
                                onUpdateAllocation(mapOf(
                                    ResourceType.FOOD to currentFood,
                                    ResourceType.WATER to newWater
                                ))
                            },
                            modifier = Modifier.width(32.dp)
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = currentWater.toString())
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateAllocation(mapOf(
                                    ResourceType.FOOD to currentFood,
                                    ResourceType.WATER to currentWater + 1
                                ))
                            },
                            modifier = Modifier.width(32.dp)
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        }
    }
}

package com.example.hydratrack.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydratrack.data.LogMethod
import com.example.hydratrack.sensor.ShakeDetector
import com.example.hydratrack.ui.theme.SuccessGreen
import com.example.hydratrack.ui.theme.WaterBlue
import com.example.hydratrack.utils.PreferencesManager

@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    val shakeDetector = remember {
        ShakeDetector(context) {
            viewModel.logWater(LogMethod.SHAKE)
        }.also { it.setThreshold(prefs.shakeSensitivity) }
    }

    DisposableEffect(Unit) {
        shakeDetector.start()
        viewModel.refreshServing()
        onDispose { shakeDetector.stop() }
    }

    val progress = if (state.dailyGoalMl > 0) {
        (state.currentIntakeMl.toFloat() / state.dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Scaffold(
        topBar = { HomeTopBar(userName = state.userName, onSettings = onNavigateToSettings) },
        bottomBar = { HomeBottomBar(onHistory = onNavigateToHistory, onSettings = onNavigateToSettings) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            WaterProgressCard(
                progress = progress,
                currentMl = state.currentIntakeMl,
                goalMl = state.dailyGoalMl,
                isGoalMet = state.isGoalMet
            )

            ServingBadge(servingMl = state.servingMl)

            ShakePromptCard(
                isGoalMet = state.isGoalMet,
                onManualLog = { viewModel.logWater(LogMethod.MANUAL) }
            )

            AnimatedVisibility(
                visible = state.canUndo,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                UndoBanner(
                    secondsLeft = state.undoCountdownSec,
                    onUndo = viewModel::undoLastLog
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(userName: String, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Good day,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
            Text(
                text = userName.ifBlank { "Hydra Hero" },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun WaterProgressCard(
    progress: Float,
    currentMl: Int,
    goalMl: Int,
    isGoalMet: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "progress_bar"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isGoalMet) "🎉 Goal Reached!" else "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                color = if (isGoalMet) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentMl",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " / $goalMl ml",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isGoalMet)
                                Brush.horizontalGradient(listOf(SuccessGreen, WaterBlue))
                            else
                                Brush.horizontalGradient(listOf(WaterBlue, MaterialTheme.colorScheme.primary))
                        )
                )
            }

            Text(
                "${(progress * 100).toInt()}% of daily goal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun ServingBadge(servingMl: Int) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("💧", fontSize = 16.sp)
            Text(
                "Serving size: $servingMl ml",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ShakePromptCard(isGoalMet: Boolean, onManualLog: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "wobble")
    val wobble by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(280), RepeatMode.Reverse),
        label = "phone_wobble"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isGoalMet) {
                Text(
                    "📱",
                    fontSize = 52.sp,
                    modifier = Modifier.offset(x = wobble.dp)
                )
                Text(
                    "Shake to log water!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Or tap the button below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            } else {
                Text("🏆", fontSize = 52.sp)
                Text(
                    "You crushed your goal today!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Button(
                onClick = onManualLog,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+ Add Glass", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun UndoBanner(secondsLeft: Int, onUndo: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Water logged! ✅  Undo in ${secondsLeft}s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onUndo) {
                Text(
                    "UNDO",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBar(onHistory: () -> Unit, onSettings: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Text("🏠", fontSize = 20.sp) },
            label = { Text("Home") },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Text("📊", fontSize = 20.sp) },
            label = { Text("History") },
            selected = false,
            onClick = onHistory
        )
        NavigationBarItem(
            icon = { Text("⚙️", fontSize = 20.sp) },
            label = { Text("Settings") },
            selected = false,
            onClick = onSettings
        )

    }
}
    }
}
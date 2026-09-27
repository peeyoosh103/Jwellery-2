package com.example.silver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.common.CustomNumberField
import com.example.common.PeeyooshTopBar
import com.example.common.PuritySelectorChips
import com.example.common.ResultActionButtons
import com.example.common.ResultSummaryCard
import com.example.common.WeightInputSection
import com.example.models.MetalType
import com.example.models.SilverPurity
import com.example.ui.theme.PeeyooshTheme
import com.example.ui.theme.PurpleGradientEnd
import com.example.ui.theme.PurpleGradientStart
import com.example.voice.VoiceControlPanel
import com.example.voice.VoiceListeningState
import kotlinx.coroutines.launch

@Composable
fun SilverCalculatorScreen(
    viewModel: SilverViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startVoiceListening()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission denied. Voice control requires mic access.")
            }
        }
    }

    val purpleGradient = Brush.horizontalGradient(
        colors = listOf(PurpleGradientStart, PurpleGradientEnd)
    )

    BackHandler {
        if (voiceState.state == VoiceListeningState.LISTENING) {
            viewModel.stopVoiceListening()
        } else if (uiState.showResult) {
            viewModel.editCalculation()
        } else {
            onNavigateBack()
        }
    }

    PeeyooshTheme(themeMode = settings.themeMode, metalTheme = MetalType.SILVER) {
        Scaffold(
            topBar = {
                PeeyooshTopBar(
                    title = "Silver Calculator",
                    subtitle = "Quick Price & Tax Breakdown",
                    onBackClick = {
                        if (uiState.showResult) {
                            viewModel.editCalculation()
                        } else {
                            onNavigateBack()
                        }
                    },
                    actions = {
                        val isListening = voiceState.state == VoiceListeningState.LISTENING
                        IconButton(
                            onClick = {
                                if (!settings.isVoiceControlEnabled) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Voice Control is disabled in Settings.")
                                    }
                                } else if (!hasMicPermission) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isListening) viewModel.stopVoiceListening() else viewModel.startVoiceListening()
                                }
                            },
                            modifier = Modifier.testTag("top_bar_silver_voice_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                !settings.isVoiceControlEnabled -> MaterialTheme.colorScheme.surfaceVariant
                                isListening -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (!settings.isVoiceControlEnabled) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Voice Control",
                                        tint = when {
                                        !settings.isVoiceControlEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        isListening -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🎙️ STEP 3 & STEP 7: Voice Control Panel
                    VoiceControlPanel(
                        voiceState = voiceState,
                        settings = settings,
                        metalType = MetalType.SILVER,
                        hasMicPermission = hasMicPermission,
                        onRequestMicPermission = {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStartListening = viewModel::startVoiceListening,
                        onStopListening = viewModel::stopVoiceListening,
                        onExampleClicked = { example ->
                            viewModel.processVoiceCommand(example)
                        }
                    )

                    // 🟣 Purity Selector (999, 925)
                    PuritySelectorChips(
                        options = SilverPurity.entries,
                        selected = uiState.selectedPurity,
                        onSelect = viewModel::onPuritySelect,
                        labelProvider = { it.label },
                        finenessProvider = { it.fineness }
                    )

                    // Weight Section (Gram + Milligram)
                    WeightInputSection(
                        grams = uiState.gramsText,
                        onGramsChange = viewModel::onGramsChange,
                        milligrams = uiState.milligramsText,
                        onMilligramsChange = viewModel::onMilligramsChange,
                        metalTheme = MetalType.SILVER
                    )

                    // Market Silver Rate (₹ per 1 kg)
                    CustomNumberField(
                        value = uiState.marketRateText,
                        onValueChange = viewModel::onMarketRateChange,
                        label = "Silver Market Rate (₹ per 1 kg)",
                        hint = "e.g. 120000",
                        suffixText = "₹/kg",
                        explanation = "Enter market rate for 1 kilogram",
                        testTag = "input_silver_rate"
                    )

                    // Charges Row: Making Charge & Wastage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomNumberField(
                                value = uiState.makingChargeText,
                                onValueChange = viewModel::onMakingChargeChange,
                                label = "Making Charge (%)",
                                hint = "5.0",
                                suffixText = "%",
                                explanation = "Gehna banane ka charge",
                                testTag = "input_silver_making"
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            CustomNumberField(
                                value = uiState.wastageText,
                                onValueChange = viewModel::onWastageChange,
                                label = "Wastage (%)",
                                hint = "0.0",
                                suffixText = "%",
                                explanation = "Wastage charge (optional)",
                                testTag = "input_silver_wastage"
                            )
                        }
                    }

                    // GST (%)
                    CustomNumberField(
                        value = uiState.gstText,
                        onValueChange = viewModel::onGstChange,
                        label = "GST (%)",
                        hint = "3.0",
                        suffixText = "%",
                        explanation = "Standard jewellery GST is 3%",
                        testTag = "input_silver_gst"
                    )

                    // Error Message if any
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Calculate Button
                    Button(
                        onClick = viewModel::calculateSilverPrice,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_calculate_silver_price")
                    ) {
                        Text(
                            text = "CALCULATE SILVER PRICE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Result Section
                    AnimatedVisibility(
                        visible = uiState.showResult && uiState.result != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.result?.let { res ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ResultSummaryCard(
                                    result = res,
                                    isRounding = settings.isRoundingEnabled,
                                    gradientBrush = purpleGradient
                                )

                                ResultActionButtons(
                                    onRecalculate = viewModel::resetCalculation,
                                    onReadResult = viewModel::speakResult,
                                    onSave = {
                                        viewModel.saveCalculation()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Saved to History successfully")
                                        }
                                    },
                                    onShare = { viewModel.shareCalculation(context) },
                                    onEdit = viewModel::editCalculation,
                                    isSaved = uiState.isSaved
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

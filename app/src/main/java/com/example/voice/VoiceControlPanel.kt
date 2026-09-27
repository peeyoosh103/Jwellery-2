package com.example.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.MetalType
import com.example.settings.AppSettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceControlPanel(
    voiceState: VoiceUiState,
    settings: AppSettings,
    metalType: MetalType,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onExampleClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val isListening = voiceState.state == VoiceListeningState.LISTENING
    val isProcessing = voiceState.state == VoiceListeningState.PROCESSING
    val isVoiceEnabled = settings.isVoiceControlEnabled

    val cardBorderColor = when {
        !isVoiceEnabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        isListening -> MaterialTheme.colorScheme.primary
        isProcessing -> MaterialTheme.colorScheme.secondary
        voiceState.feedbackMessage != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.5.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isListening) 4.dp else 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Title, Continuous status, and Big Voice Mic Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Microphone Activation Circle Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(52.dp)
                    ) {
                        if (isListening) {
                            // Pulsing halo wave
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = when {
                                !isVoiceEnabled -> MaterialTheme.colorScheme.surfaceVariant
                                isListening -> MaterialTheme.colorScheme.primary
                                isProcessing -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            contentColor = when {
                                !isVoiceEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                isListening || isProcessing -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(
                                    enabled = isVoiceEnabled,
                                    onClick = {
                                        if (!hasMicPermission) {
                                            onRequestMicPermission()
                                        } else {
                                            if (isListening) onStopListening() else onStartListening()
                                        }
                                    }
                                )
                                .testTag("btn_voice_mic")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (!isVoiceEnabled) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Voice Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (settings.isContinuousListening && isVoiceEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Continuous",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isListening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when {
                                !isVoiceEnabled -> "Voice Control is OFF in Settings"
                                !hasMicPermission -> "Tap mic to allow microphone permission"
                                isListening -> "Listening in Hindi/English... Bolna shuru karein"
                                isProcessing -> "Processing voice command..."
                                else -> "Tap mic & speak: \"5g gold, rate 1 lakh\""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                !isVoiceEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                isListening -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isListening) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                if (isListening) {
                    IconButton(
                        onClick = onStopListening,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_voice_stop")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Listening",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Live Partial or Final Speech Transcription Box
            AnimatedVisibility(
                visible = voiceState.partialTranscript.isNotBlank() || isListening,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (voiceState.partialTranscript.isNotBlank()) {
                                "“${voiceState.partialTranscript}”"
                            } else {
                                "Listening for weight, rate, making..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Feedback / Applied Action Chip Banner
            AnimatedVisibility(
                visible = !voiceState.feedbackMessage.isNullOrBlank() && !isListening,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Applied:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = voiceState.feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Quick Example Pills
            if (isVoiceEnabled && !isListening && hasMicPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Spoken Examples (Hindi / English):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val examples = if (metalType == MetalType.GOLD) {
                        listOf(
                            "5 gram sona, rate 1 lakh",
                            "Rate 98 hazar 10 gram ka",
                            "Making 8 percent laga do",
                            "Calculate karo",
                            "Reset karo"
                        )
                    } else {
                        listOf(
                            "250 gram chandi, rate 1 lakh 20 hazar",
                            "Rate 120000 per kg",
                            "Making 5 percent",
                            "Calculate karo",
                            "Reset karo"
                        )
                    }

                    examples.forEach { example ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onExampleClicked(example) }
                        ) {
                            Text(
                                text = "“$example”",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

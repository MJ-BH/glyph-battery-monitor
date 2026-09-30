package com.nothing.glyphbattery.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.domain.model.MorseMessage
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun MorseStudioCard(
    isTransmitting: Boolean,
    currentText: String?,
    onTransmitSos: () -> Unit,
    onTransmitCustomText: (String) -> Unit,
    onCancelTransmission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customInput by remember { mutableStateOf("NOTHING") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "  OPTICAL MORSE CODE SOS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 1.sp
                    )
                }

                if (isTransmitting) {
                    Text(
                        text = "TRANSMITTING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed
                    )
                }
            }

            // High Priority SOS Button
            Button(
                onClick = {
                    if (isTransmitting) onCancelTransmission() else onTransmitSos()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTransmitting) NothingWhite else NothingRed,
                    contentColor = if (isTransmitting) NothingRed else NothingWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = if (isTransmitting) Icons.Default.Cancel else Icons.Default.Emergency,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTransmitting) "STOP MORSE TRANSMISSION" else "TRANSMIT EMERGENCY SOS (... --- ...)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Custom Text Morse Encoder
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TRANSMIT CUSTOM TEXT TO GLYPH LEDS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NothingWhiteMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it.take(20).uppercase() },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NothingRed,
                            unfocusedBorderColor = NothingCardBorder,
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onTransmitCustomText(customInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingCardBorder,
                            contentColor = NothingWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                // Morse Code Translation Preview
                if (customInput.isNotBlank()) {
                    Text(
                        text = "Morse Sequence: ${MorseMessage.encodeToMorse(customInput)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingRed
                    )
                }
            }
        }
    }
}

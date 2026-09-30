package com.nothing.glyphbattery.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun NothingHeader(
    currentModel: NothingDeviceModel,
    isGlyphConnected: Boolean,
    onModelSelected: (NothingDeviceModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NothingRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOTHING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    text = "GLYPH BATTERY MONITOR",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NothingWhiteMuted,
                    letterSpacing = 1.sp
                )
            }

            // Model Switcher Chip
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(NothingDarkSurface)
                        .border(1.dp, NothingCardBorder, RoundedCornerShape(14.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (isGlyphConnected) NothingWhite else NothingWhiteMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentModel) {
                            NothingDeviceModel.PHONE_2A -> "PHONE (2a)"
                            NothingDeviceModel.PHONE_2A_PLUS -> "PHONE (2a)+"
                            NothingDeviceModel.PHONE_2 -> "PHONE (2)"
                            NothingDeviceModel.PHONE_1 -> "PHONE (1)"
                            NothingDeviceModel.PHONE_3A_SERIES -> "PHONE (3a/4a)"
                            NothingDeviceModel.VIRTUAL_SIMULATOR -> "SIMULATOR"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(NothingDarkSurface)
                ) {
                    NothingDeviceModel.entries.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model.displayName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (model == currentModel) NothingRed else NothingWhite
                                )
                            },
                            onClick = {
                                onModelSelected(model)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

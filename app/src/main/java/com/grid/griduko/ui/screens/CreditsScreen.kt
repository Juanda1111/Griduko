package com.grid.griduko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.audio.SoundManager
import com.grid.griduko.data.CreditsInfo
import com.grid.griduko.data.GameStorage
import com.grid.griduko.ui.components.GridukoLogo
import com.grid.griduko.ui.components.GridukoTopBar
import com.grid.griduko.ui.theme.AccentYellow
import com.grid.griduko.ui.theme.NavyBackground
import com.grid.griduko.ui.theme.NavyBorder
import com.grid.griduko.ui.theme.NavySurface
import com.grid.griduko.ui.theme.TextDarkNavy
import com.grid.griduko.ui.theme.TextMuted
import com.grid.griduko.ui.theme.TextWhite

@Composable
fun CreditsScreen(
    creditsInfo: CreditsInfo,
    easterEggsFound: Int,
    rainbowModeUnlocked: Boolean,
    storage: GameStorage,
    soundManager: SoundManager,
    onBackToHome: () -> Unit
) {
    var editingField by remember { mutableStateOf<String?>(null) }
    var editingValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridukoTopBar(
                title = "Créditos",
                showLogo = false,
                onBackClick = {
                    soundManager.playClick()
                    onBackToHome()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            GridukoLogo(
                size = 96.dp,
                rainbowMode = rainbowModeUnlocked,
                animatePulse = true,
                onLogoClick = {
                    soundManager.playEasterEgg()
                    storage.registerEasterEggFound()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Griduko",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Versión 1.0",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CreditInfoCard(
                    label = "DESARROLLO",
                    value = creditsInfo.developerName,
                    onClick = {
                        soundManager.playClick()
                        editingField = "DESARROLLO"
                        editingValue = creditsInfo.developerName
                    }
                )

                CreditInfoCard(
                    label = "DISEÑO",
                    value = creditsInfo.designerName,
                    onClick = {
                        soundManager.playClick()
                        editingField = "DISEÑO"
                        editingValue = creditsInfo.designerName
                    }
                )

                CreditInfoCard(
                    label = "JUEGOS",
                    value = "Snake · 2048 · Tres en raya",
                    onClick = null
                )

                CreditInfoCard(
                    label = "AGRADECIMIENTOS",
                    value = if (easterEggsFound > 0) {
                        "${creditsInfo.acknowledgements} (Easter Eggs descubiertos: $easterEggsFound)"
                    } else {
                        creditsInfo.acknowledgements
                    },
                    onClick = {
                        soundManager.playClick()
                        editingField = "AGRADECIMIENTOS"
                        editingValue = creditsInfo.acknowledgements
                    }
                )
            }
        }

        Button(
            onClick = {
                soundManager.playClick()
                onBackToHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentYellow,
                contentColor = TextDarkNavy
            )
        ) {
            Text(
                text = "Volver al inicio",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    if (editingField != null) {
        AlertDialog(
            onDismissRequest = { editingField = null },
            containerColor = NavySurface,
            titleContentColor = TextWhite,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = "Editar $editingField",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = editingValue,
                    onValueChange = { editingValue = it },
                    singleLine = editingField != "AGRADECIMIENTOS",
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editingValue.trim().ifEmpty { "Juan David Londoño" }
                        val updated = when (editingField) {
                            "DESARROLLO" -> creditsInfo.copy(developerName = trimmed)
                            "DISEÑO" -> creditsInfo.copy(designerName = trimmed)
                            "AGRADECIMIENTOS" -> creditsInfo.copy(acknowledgements = trimmed)
                            else -> creditsInfo
                        }
                        storage.saveCreditsInfo(updated)
                        editingField = null
                    }
                ) {
                    Text("Guardar", color = AccentYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun CreditInfoCard(
    label: String,
    value: String,
    onClick: (() -> Unit)?
) {
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .then(clickMod)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = AccentYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

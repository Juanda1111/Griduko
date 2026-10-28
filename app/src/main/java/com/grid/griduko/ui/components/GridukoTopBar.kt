package com.grid.griduko.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.griduko.ui.theme.TextWhite

/**
 * Barra superior común para las pantallas de juego y créditos:
 * - Botón atrás circular (40.dp) a la izquierda
 * - Logo de la app (40.dp, opcional según pantalla) a la izquierda del título central (24.sp)
 * - Botón de acción (pausa/reiniciar, 40.dp) a la derecha
 */
@Composable
fun GridukoTopBar(
    title: String,
    onBackClick: () -> Unit,
    rightActionSymbol: String? = null,
    onRightActionClick: (() -> Unit)? = null,
    showLogo: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleActionButton(
            symbol = "←",
            onClick = onBackClick
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showLogo) {
                GridukoLogo(
                    size = 40.dp,
                    animatePulse = false
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (rightActionSymbol != null && onRightActionClick != null) {
            CircleActionButton(
                symbol = rightActionSymbol,
                onClick = onRightActionClick
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

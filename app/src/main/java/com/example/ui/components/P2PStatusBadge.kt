package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.p2p.P2PConnectionState
import com.example.data.p2p.P2PSyncStatus
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

import androidx.compose.ui.text.style.TextOverflow

@Composable
fun P2PStatusBadge(
    status: P2PSyncStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val EmeraldPrimary = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "p2p_badge_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (badgeColor, labelText) = when {
        !status.isEnabled -> Pair(Color.Gray, "P2P Off")
        status.state == P2PConnectionState.CONNECTED -> Pair(IncomeGreen, "P2P (${status.connectedPeers.size})")
        status.state == P2PConnectionState.SYNCING -> Pair(EmeraldPrimary, "Sinc...")
        status.state == P2PConnectionState.SEARCHING -> Pair(Color(0xFFFFB300), "Buscando")
        status.state == P2PConnectionState.ERROR -> Pair(ExpenseRed, "Erro")
        else -> Pair(Color.Gray, "P2P")
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = badgeColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag("p2p_status_badge"),
        color = badgeColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .then(
                        if (status.state == P2PConnectionState.SEARCHING || status.state == P2PConnectionState.SYNCING) {
                            Modifier.alpha(pulseAlpha)
                        } else Modifier
                    )
            )

            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "P2P Sync",
                tint = badgeColor,
                modifier = Modifier.size(13.dp)
            )

            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


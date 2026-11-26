package com.example.mycycle.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mycycle.R

/** Сетка аватарок 4 в ряд с одиночным выбором. */
@Composable
fun AvatarPicker(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.cd_avatar)
    Column(modifier) {
        AVATAR_KEYS.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { key ->
                    val on = key == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = if (on) 3.dp else 1.dp,
                                color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(key) }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(avatarRes(key)),
                            contentDescription = cd,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    }
                }
            }
        }
    }
}

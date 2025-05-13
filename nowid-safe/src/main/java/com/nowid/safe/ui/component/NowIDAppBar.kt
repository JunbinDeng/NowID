package com.nowid.safe.ui.component

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun NowIDAppBar(
    modifier: Modifier = Modifier,
    titleText: String? = null,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    colors: Color = MaterialTheme.colors.primary.copy(alpha = 0.03f),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    TopAppBar(
        title = { titleText?.let { Text(titleText) } },
        navigationIcon = {
            navigationIcon?.let {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconContentDescription,
                    )
                }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription,
                    )
                }
            }
        },
        modifier = modifier.testTag("NowIDTopAppBar"),
        backgroundColor = colors,
        elevation = 0.dp,
    )
}
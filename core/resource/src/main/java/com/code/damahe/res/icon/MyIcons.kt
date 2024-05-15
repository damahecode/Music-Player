package com.code.damahe.res.icon

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

/**
 * Damahe Code icons. Material icons are [ImageVector]s, custom icons are drawable resource IDs.
 */
object MyIcons {
    val Settings = Icons.Rounded.Settings
    val Info = Icons.Default.Info
}

/**
 * A sealed class to make dealing with [ImageVector] and [DrawableRes] icons easier.
 */
sealed class DCodeIcon {
    data class ImageVectorIcon(val imageVector: ImageVector) : DCodeIcon()
    data class DrawableResourceIcon(@DrawableRes val id: Int) : DCodeIcon()
}

/**
 * A Material Design icon component that draws [DCodeIcon] using [tint], with a default value of [LocalContentColor].
 */
@Composable
fun DrawIcon(
    icon: DCodeIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    when (icon) {
        is DCodeIcon.ImageVectorIcon -> Icon(
            imageVector = icon.imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )

        is DCodeIcon.DrawableResourceIcon -> Icon(
            painter = painterResource(id = icon.id),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}
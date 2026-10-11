package com.iqbalansyori.izzah.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme
import com.iqbalansyori.izzah.designsystem.theme.IzzahType

@Composable
fun WelcomeScreen(onStart: () -> Unit, onPin: () -> Unit) {
    val titleSize = 42.sp
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg)) {
        Box(
            Modifier.align(Alignment.TopStart).offset((-30).dp, 76.dp).size(180.dp, 220.dp).rotate(-9f)
                .clip(RoundedCornerShape(26.dp)).background(IzzahColors.Card),
        ) { IllustrationCard(Illustration.SUNRISE) }
        Box(
            Modifier.align(Alignment.TopEnd).offset(30.dp, 76.dp).size(180.dp, 220.dp).rotate(9f)
                .clip(RoundedCornerShape(26.dp)).background(IzzahColors.Card),
        ) { IllustrationCard(Illustration.TREE) }
        Box(
            Modifier.align(Alignment.TopCenter).offset(y = 60.dp).size(190.dp, 250.dp)
                .shadow(30.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(IzzahColors.Tile),
        ) { IllustrationCard(Illustration.HIKER) }
        Box(
            Modifier.fillMaxWidth().offset(y = 240.dp).height(100.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, IzzahColors.Bg))),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 28.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StarLogo(22.dp, stroke = 2.dp)
                Text(stringResource(R.string.app_name), style = IzzahType.outfit(19.sp, FontWeight.Bold))
            }
            Text(
                stringResource(R.string.welcome_title),
                style = IzzahType.outfit(titleSize, FontWeight.Bold, tracking = -.035f, lineHeight = 1.04f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.width(18.dp).height(1.dp).background(IzzahColors.Text.copy(alpha = .3f)))
                Text(stringResource(R.string.welcome_quote), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.TextSecondary)
            }
            Text(
                stringResource(R.string.welcome_body),
                style = IzzahType.outfit(16.sp, FontWeight.Normal, lineHeight = 1.5f),
                color = IzzahColors.TextSecondary,
            )
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(stringResource(R.string.welcome_cta_start), onStart)
                GhostButtonQuiet(stringResource(R.string.welcome_cta_pin), onPin)
            }
            Text(
                stringResource(R.string.welcome_footnote),
                Modifier.fillMaxWidth(),
                style = IzzahType.outfit(12.5.sp, FontWeight.Normal),
                color = IzzahColors.Footnote,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GhostButtonQuiet(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(48.dp).clip(IzzahShapes.Button).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IzzahType.outfit(15.sp, FontWeight.Medium), color = IzzahColors.Text)
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF0E0E11)
@Composable
private fun WelcomePreview() = IzzahTheme { WelcomeScreen({}, {}) }

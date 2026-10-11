package com.iqbalansyori.izzah.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.StarLogo
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahTheme
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(Unit) {
        delay(2200)
        done()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(0f to IzzahColors.SplashCenter, .62f to IzzahColors.Bg, center = androidx.compose.ui.geometry.Offset.Unspecified))
            .clickable(onClick = onDone),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
            StarLogo(96.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = IzzahType.outfit(34.sp, FontWeight.Bold, tracking = -.02f))
                Text(stringResource(R.string.app_name_arabic), style = IzzahType.arabic(22.sp), color = IzzahColors.TextSecondary)
            }
        }
        Text(
            stringResource(R.string.splash_tagline),
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 56.dp),
            style = IzzahType.outfit(13.sp, FontWeight.Normal, tracking = .04f),
            color = IzzahColors.Footnote,
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF0E0E11)
@Composable
private fun SplashPreview() = IzzahTheme { SplashScreen({}) }

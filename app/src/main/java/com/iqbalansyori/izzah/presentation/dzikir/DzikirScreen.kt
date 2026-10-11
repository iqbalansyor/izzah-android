package com.iqbalansyori.izzah.presentation.dzikir

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.BackButton
import com.iqbalansyori.izzah.designsystem.component.ConicRing
import com.iqbalansyori.izzah.designsystem.component.pressScale
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.presentation.navigation.VolumeKeyBus
import org.koin.androidx.compose.koinViewModel

private data class Step(val arabic: Int, val translit: Int, val meaning: Int)

private val STEPS = listOf(
    Step(R.string.dzikir_1, R.string.dzikir_1_translit, R.string.dzikir_1_meaning),
    Step(R.string.dzikir_2, R.string.dzikir_2_translit, R.string.dzikir_2_meaning),
    Step(R.string.dzikir_3, R.string.dzikir_3_translit, R.string.dzikir_3_meaning),
)

@Composable
fun DzikirRoot(onBack: () -> Unit, vm: DzikirViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // the volume keys count taps only while this screen is up, and keep their normal job elsewhere
    DisposableEffect(Unit) {
        VolumeKeyBus.listening = true
        onDispose { VolumeKeyBus.listening = false }
    }
    LaunchedEffect(Unit) { VolumeKeyBus.events.collect { vm.tap() } }
    LaunchedEffect(Unit) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vm.haptic.collect { vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }
    DzikirScreen(state, onTap = vm::tap, onReset = vm::reset, onBack = onBack)
}

@Composable
fun DzikirScreen(state: DzikirState, onTap: () -> Unit, onReset: () -> Unit, onBack: () -> Unit) {
    val current = STEPS[state.set.coerceAtMost(STEPS.lastIndex)]
    Column(
        Modifier.fillMaxSize().background(IzzahColors.Bg).statusBarsPadding().navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text(stringResource(R.string.dzikir_title), style = IzzahType.outfit(18.sp, FontWeight.Bold))
            ResetButton(onReset)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            STEPS.forEachIndexed { i, step ->
                val done = state.set > i
                val active = state.set == i
                Text(
                    stringResource(step.translit) + if (done) " ✓" else "",
                    Modifier.clip(IzzahShapes.R12).background(if (active) IzzahColors.Text else IzzahColors.Selected).padding(horizontal = 12.dp, vertical = 7.dp),
                    style = IzzahType.outfit(12.5.sp, FontWeight.SemiBold),
                    color = if (active) IzzahColors.Bg else if (done) IzzahColors.Lavender else IzzahColors.TextSecondary,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(current.arabic), style = IzzahType.arabic(44.sp).copy(lineHeight = 57.sp), textAlign = TextAlign.Center)
            Text(stringResource(current.translit), style = IzzahType.outfit(18.sp, FontWeight.SemiBold))
            Text(stringResource(current.meaning), style = IzzahType.outfit(14.sp, FontWeight.Normal), color = IzzahColors.Meta)
        }
        val interaction = remember { MutableInteractionSource() }
        ConicRing(
            state.progress,
            Modifier.pressScale(interaction, .97f).clip(CircleShape).clickable(interaction, null, role = Role.Button, onClick = onTap),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (state.finished) "99" else "${state.count}",
                    style = IzzahType.outfit(72.sp, FontWeight.ExtraBold, tracking = -.04f, lineHeight = 1f),
                )
                Text(
                    stringResource(if (state.finished) R.string.dzikir_done else R.string.dzikir_hint),
                    style = IzzahType.outfit(14.sp, FontWeight.Normal),
                    color = IzzahColors.Meta,
                )
            }
        }
        Text(
            stringResource(R.string.dzikir_footer),
            Modifier.widthIn(max = 280.dp),
            style = IzzahType.outfit(13.5.sp, FontWeight.Normal, lineHeight = 1.45f),
            color = IzzahColors.Footnote,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResetButton(onReset: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(IzzahShapes.R14).background(IzzahColors.Tile).clickable(role = Role.Button, onClick = onReset),
        contentAlignment = Alignment.Center,
    ) {
        Icon(IzzahGlyphs.Reset, stringResource(R.string.dzikir_reset_cd), Modifier.size(20.dp), tint = IzzahColors.Text)
    }
}

package com.iqbalansyori.izzah.presentation.mulai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.designsystem.component.IzzahChip
import com.iqbalansyori.izzah.designsystem.component.PrimaryButton
import com.iqbalansyori.izzah.designsystem.icons.IzzahGlyphs
import com.iqbalansyori.izzah.designsystem.theme.IzzahColors
import com.iqbalansyori.izzah.designsystem.theme.IzzahShapes
import com.iqbalansyori.izzah.designsystem.theme.IzzahType
import com.iqbalansyori.izzah.domain.model.Trigger
import com.iqbalansyori.izzah.presentation.common.label

private val STEPS = listOf(
    R.string.mulai_step_1 to R.string.mulai_step_1_desc,
    R.string.mulai_step_2 to R.string.mulai_step_2_desc,
    R.string.mulai_step_3 to R.string.mulai_step_3_desc,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MulaiScreen(state: MulaiState, onAction: (MulaiAction) -> Unit, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(IzzahColors.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(IzzahShapes.R14).background(IzzahColors.Tile).clickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(IzzahGlyphs.Close, stringResource(R.string.mulai_close_cd), Modifier.size(20.dp), tint = IzzahColors.Text) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.mulai_title), style = IzzahType.outfit(34.sp, FontWeight.Bold, tracking = -.03f, lineHeight = 1.08f))
                Text(
                    stringResource(R.string.mulai_body, state.kept),
                    style = IzzahType.outfit(16.sp, FontWeight.Normal, lineHeight = 1.5f),
                    color = IzzahColors.TextSecondary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                STEPS.forEachIndexed { i, (title, desc) ->
                    val on = state.steps[i]
                    Row(
                        Modifier.fillMaxWidth().clip(IzzahShapes.Button).background(IzzahColors.Card).border(1.dp, IzzahColors.Border, IzzahShapes.Button)
                            .clickable(role = Role.Checkbox) { onAction(MulaiAction.ToggleStep(i)) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            Modifier.size(32.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .background(if (on) IzzahColors.BarKept else IzzahColors.Selected),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (on) Icon(IzzahGlyphs.Check, null, Modifier.size(16.dp), tint = IzzahColors.Bg)
                            else Text("${i + 1}", style = IzzahType.outfit(14.sp, FontWeight.ExtraBold), color = IzzahColors.TextSecondary)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(title), style = IzzahType.outfit(16.sp, FontWeight.SemiBold))
                            Text(stringResource(desc), style = IzzahType.outfit(13.sp, FontWeight.Normal), color = IzzahColors.Meta)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.mulai_triggers_title).uppercase(),
                    style = IzzahType.outfit(12.sp, FontWeight.SemiBold, tracking = .12f),
                    color = IzzahColors.Meta,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Trigger.entries.forEach { t ->
                        IzzahChip(stringResource(t.label), t in state.triggers, { onAction(MulaiAction.ToggleTrigger(t)) }, compact = true)
                    }
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(130.dp)
                .background(Brush.verticalGradient(0f to Color.Transparent, .4f to IzzahColors.Bg)),
        )
        PrimaryButton(
            stringResource(R.string.mulai_cta),
            { onAction(MulaiAction.Confirm) },
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 30.dp),
        )
    }
}

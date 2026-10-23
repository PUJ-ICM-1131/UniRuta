package com.uniruta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.AuthMode
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.data.model.UserRole
import com.uniruta.app.ui.theme.BadgeStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun RoleHomeScaffold(
    user: SessionUser,
    headline: String,
    features: List<FeatureItem>,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    sectionLabel: String = "Acciones",
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                HomeHeader(user = user, headline = headline)
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    header?.invoke(this)
                    SectionLabel(text = sectionLabel)
                    features.forEach { feature -> FeatureCard(item = feature) }
                    footer?.invoke(this)
                }
            }
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
            ) {
                SecondaryButton(text = "Cerrar sesión", onClick = onLogout, icon = UniRutaIcons.Logout)
            }
        }
        StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun HomeHeader(user: SessionUser, headline: String) {
    Surface(
        color = UniRutaTheme.colors.headerContainer,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandMark(size = 40.dp, iconSize = 22.dp)
                Text(
                    text = "UniRuta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                )
                RoleBadge(role = user.role)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Hola, ${user.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = UniRutaTheme.colors.headerSubtitle
                )
                SessionModeLabel(mode = user.mode)
            }
        }
    }
}

@Composable
private fun SessionModeLabel(mode: AuthMode) {
    val tone = if (mode == AuthMode.DEMO) StatusTone.WARNING else StatusTone.POSITIVE
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusChip(
            label = if (mode == AuthMode.DEMO) "Modo demostración" else "Sesión verificada con Firebase",
            tone = tone
        )
    }
}

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = UniRutaIcons.Bus,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val extended = UniRutaTheme.colors
    Surface(
        color = extended.warningContainer,
        contentColor = extended.onWarningContainer,
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = role.icon(), contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = role.label.uppercase(), style = BadgeStyle)
        }
    }
}

fun UserRole.icon(): ImageVector = when (this) {
    UserRole.STUDENT -> UniRutaIcons.GraduationCap
    UserRole.DRIVER -> UniRutaIcons.SteeringWheel
    UserRole.ADMIN -> UniRutaIcons.ShieldCheck
}

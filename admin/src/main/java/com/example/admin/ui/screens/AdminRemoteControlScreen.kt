package com.example.admin.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.admin.data.AdminRoleItem
import com.example.admin.data.RemoteAppConfig
import com.example.admin.data.UserControlItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminDanger
import com.example.admin.ui.theme.AdminElectricBlue
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminRoyalPurple
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminSurfaceElevated
import com.example.admin.ui.theme.AdminTextSecondary
import com.example.admin.ui.theme.AdminWarning

@Composable
fun AdminRemoteControlScreen(
    config: RemoteAppConfig,
    users: List<UserControlItem>,
    admins: List<AdminRoleItem>,
    onSaveGlobalConfig: (RemoteAppConfig) -> Unit,
    onUpdateUserAccess: (UserControlItem, Boolean, Boolean) -> Unit,
    onAddAdmin: (String, String, String, String) -> Unit,
    onRemoveAdmin: (String) -> Unit
) {
    var draftConfig by remember { mutableStateOf(config) }
    LaunchedEffect(config) {
        draftConfig = config
    }

    var userQuery by remember { mutableStateOf("") }
    var newAdminUid by remember { mutableStateOf("") }
    var newAdminEmail by remember { mutableStateOf("") }
    var newAdminName by remember { mutableStateOf("") }

    val filteredUsers = remember(users, userQuery) {
        val q = userQuery.trim().lowercase()
        if (q.isEmpty()) users
        else users.filter {
            it.displayName.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.userId.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_remote_control_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Global User App Remote Flags",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Changes push instantly to all installed User APKs without rebuilding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminTextSecondary
                    )

                    ConfigSwitchRow(
                        label = "Maintenance Mode (Lock User App)",
                        checked = draftConfig.maintenanceMode,
                        onCheckedChange = { draftConfig = draftConfig.copy(maintenanceMode = it) }
                    )
                    OutlinedTextField(
                        value = draftConfig.maintenanceMessage,
                        onValueChange = { draftConfig = draftConfig.copy(maintenanceMessage = it.take(240)) },
                        label = { Text("Maintenance Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftConfig.announcementBanner,
                        onValueChange = { draftConfig = draftConfig.copy(announcementBanner = it.take(240)) },
                        label = { Text("Home Announcement Banner (blank = hidden)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ConfigSwitchRow(
                        label = "AI Search & Phone Content Search",
                        checked = draftConfig.aiSearchEnabled,
                        onCheckedChange = { draftConfig = draftConfig.copy(aiSearchEnabled = it) }
                    )
                    ConfigSwitchRow(
                        label = "Live Video Wallpapers Engine",
                        checked = draftConfig.liveWallpapersEnabled,
                        onCheckedChange = { draftConfig = draftConfig.copy(liveWallpapersEnabled = it) }
                    )
                    ConfigSwitchRow(
                        label = "Quick Shortcuts Edge Panel",
                        checked = draftConfig.edgePanelEnabled,
                        onCheckedChange = { draftConfig = draftConfig.copy(edgePanelEnabled = it) }
                    )
                    ConfigSwitchRow(
                        label = "Animated Control Center Panel",
                        checked = draftConfig.controlCenterEnabled,
                        onCheckedChange = { draftConfig = draftConfig.copy(controlCenterEnabled = it) }
                    )
                    ConfigSwitchRow(
                        label = "App Notification Badges",
                        checked = draftConfig.notificationBadgesEnabled,
                        onCheckedChange = { draftConfig = draftConfig.copy(notificationBadgesEnabled = it) }
                    )

                    Button(
                        onClick = { onSaveGlobalConfig(draftConfig) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_global_config_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Push Live Config to User App")
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Admin Role Access (/admins)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = newAdminUid,
                        onValueChange = { newAdminUid = it.take(128) },
                        label = { Text("Firebase Auth UID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it.take(120) },
                        label = { Text("Google Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAdminName,
                        onValueChange = { newAdminName = it.take(60) },
                        label = { Text("Admin Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilledTonalButton(
                        onClick = {
                            onAddAdmin(newAdminUid, newAdminEmail, newAdminName, "CONTENT_ADMIN")
                            newAdminUid = ""
                            newAdminEmail = ""
                            newAdminName = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Authorize Admin")
                    }

                    admins.forEach { admin ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(admin.displayName, color = Color.White, fontWeight = FontWeight.Medium)
                                Text("${admin.email} · ${admin.role}", color = AdminTextSecondary, style = MaterialTheme.typography.labelMedium)
                            }
                            IconButton(onClick = { onRemoveAdmin(admin.adminUid) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove Admin", tint = AdminDanger)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Registered Launcher Users (${filteredUsers.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                label = { Text("Search users by name, email or UID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredUsers, key = { it.userId }) { user ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AdminSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(user.displayName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            user.email.ifBlank { user.userId },
                            color = AdminTextSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = buildString {
                                append(if (user.premiumActive) "PRO (${user.purchaseSource})" else "FREE")
                                append(" · ")
                                append(if (user.isBlocked) "BLOCKED" else if (user.isActive) "ONLINE" else "OFFLINE")
                                append(" · ")
                                append(user.spinnerStyle)
                            },
                            color = when {
                                user.isBlocked -> AdminDanger
                                user.premiumActive -> AdminNeonCyan
                                else -> AdminSuccess
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                onUpdateUserAccess(user, !user.premiumActive, user.isBlocked)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = "Grant or revoke Pro",
                                tint = if (user.premiumActive) AdminElectricBlue else AdminTextSecondary
                            )
                        }
                        IconButton(
                            onClick = {
                                onUpdateUserAccess(user, user.premiumActive, !user.isBlocked)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = "Block or unblock user",
                                tint = if (user.isBlocked) AdminDanger else AdminWarning
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

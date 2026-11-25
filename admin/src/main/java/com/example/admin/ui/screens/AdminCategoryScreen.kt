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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.admin.data.CategoryItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminDanger
import com.example.admin.ui.theme.AdminRoyalPurple
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminTextSecondary

@Composable
fun AdminCategoryScreen(
    categories: List<CategoryItem>,
    onSeedDefaults: () -> Unit,
    onSaveCategory: (String?, String, String, Int, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_category_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_category_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("New Category")
                }
                FilledTonalButton(
                    onClick = onSeedDefaults,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("seed_categories_btn")
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Seed 9 Presets")
                }
            }
        }

        items(categories, key = { it.categoryId }) { cat ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${cat.name} (#${cat.sortOrder})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        if (cat.description.isNotBlank()) {
                            Text(
                                text = cat.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AdminTextSecondary
                            )
                        }
                        Text(
                            text = if (cat.isActive) "Visible in User App" else "Hidden",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (cat.isActive) AdminSuccess else AdminTextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { editingCategory = cat }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Category", tint = Color.White)
                        }
                        IconButton(onClick = { onDeleteCategory(cat.categoryId) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Category", tint = AdminDanger)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog || editingCategory != null) {
        val current = editingCategory
        var name by remember(current) { mutableStateOf(current?.name ?: "") }
        var desc by remember(current) { mutableStateOf(current?.description ?: "") }
        var sortText by remember(current) { mutableStateOf((current?.sortOrder ?: (categories.size + 1)).toString()) }
        var isActive by remember(current) { mutableStateOf(current?.isActive ?: true) }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                editingCategory = null
            },
            title = { Text(if (current == null) "Create Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(40) },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it.take(160) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sortText,
                        onValueChange = { sortText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Sort Order") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active in User App")
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCategory(
                            current?.categoryId,
                            name,
                            desc,
                            sortText.toIntOrNull() ?: 1,
                            isActive
                        )
                        showCreateDialog = false
                        editingCategory = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        editingCategory = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

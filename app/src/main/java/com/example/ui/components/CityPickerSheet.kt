package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.City
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    selectedCity: City,
    searchQuery: String,
    filteredCities: List<City>,
    onQueryChange: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickCities = listOf("İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Konya", "Mekke-i Mükerreme", "Medine-i Münevvere")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMutedLight) },
        modifier = modifier.testTag("city_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Şehir Seçimi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("İl veya ülke ara (Örn: İstanbul, Mekke...)", color = TextMutedLight) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = TextSecondaryLight)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = MidnightSurfaceVariant,
                    unfocusedContainerColor = MidnightSurfaceVariant,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick popular chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickCities) { cityName ->
                    val isCurrent = selectedCity.name == cityName
                    SuggestionChip(
                        onClick = {
                            val found = filteredCities.firstOrNull { it.name == cityName }
                            if (found != null) {
                                onCitySelected(found)
                            } else {
                                onQueryChange(cityName)
                            }
                        },
                        label = {
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) GoldSecondary else TextSecondaryLight
                                )
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isCurrent) MidnightSurfaceVariant else MidnightBackground
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isCurrent) GoldPrimary else Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtered Cities List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCities, key = { it.id }) { city ->
                    val isSelected = city.id == selectedCity.id
                    Surface(
                        onClick = { onCitySelected(city) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MidnightSurfaceVariant else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_item_${city.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = if (isSelected) GoldPrimary else TextMutedLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) GoldSecondary else TextPrimaryLight
                                        )
                                    )
                                    Text(
                                        text = city.country,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMutedLight,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = EmeraldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

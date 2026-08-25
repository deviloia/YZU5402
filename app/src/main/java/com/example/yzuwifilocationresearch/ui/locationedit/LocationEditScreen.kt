package com.example.yzuwifilocationresearch.ui.locationedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yzuwifilocationresearch.firebase.LocationRepository
import com.example.yzuwifilocationresearch.model.LocationPoint
import com.example.yzuwifilocationresearch.navigation.AppDestination
import com.example.yzuwifilocationresearch.ui.components.ActionBlue
import com.example.yzuwifilocationresearch.ui.components.AppCard
import com.example.yzuwifilocationresearch.ui.components.AppScaffold
import com.example.yzuwifilocationresearch.ui.components.BlueTint
import com.example.yzuwifilocationresearch.ui.components.CollectGreen
import com.example.yzuwifilocationresearch.ui.components.GreenTint
import com.example.yzuwifilocationresearch.ui.components.RedTint
import com.example.yzuwifilocationresearch.ui.components.SectionHeader
import com.example.yzuwifilocationresearch.ui.components.StatusPill
import com.example.yzuwifilocationresearch.ui.components.TextMuted
import com.example.yzuwifilocationresearch.ui.components.TextStrong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LocationEditScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val repository = remember { LocationRepository() }
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(LocationEditUiState(isLoading = true)) }
    var formState by remember { mutableStateOf(LocationFormState()) }

    fun selectLocation(location: LocationPoint) {
        uiState = uiState.copy(
            selectedLocation = location,
            successMessage = null,
            errorMessage = null
        )
        formState = LocationFormState.fromLocation(location)
    }

    fun loadLocations() {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)
            uiState = try {
                val locations = withContext(Dispatchers.IO) {
                    repository.getAllLocations().sortedWith(
                        compareBy<LocationPoint> { it.buildingId }
                            .thenBy { it.floorId }
                            .thenBy { it.positionName }
                            .thenBy { it.subPosition }
                            .thenBy { it.locationId }
                    )
                }
                val selected = uiState.selectedLocation
                    ?.let { current -> locations.firstOrNull { it.locationId == current.locationId } }
                    ?: locations.firstOrNull()

                if (selected != null) {
                    formState = LocationFormState.fromLocation(selected)
                }

                LocationEditUiState(
                    locations = locations,
                    selectedLocation = selected
                )
            } catch (error: Exception) {
                LocationEditUiState(errorMessage = error.message ?: "Failed to load locations")
            }
        }
    }

    fun saveSelectedLocation() {
        val selected = uiState.selectedLocation ?: return
        val requiredFieldError = formState.requiredFieldError()
        val latitude = formState.manualLatitude.toNullableDoubleOrError("manualLatitude")
        val longitude = formState.manualLongitude.toNullableDoubleOrError("manualLongitude")

        if (requiredFieldError != null || latitude.isFailure || longitude.isFailure) {
            uiState = uiState.copy(
                errorMessage = requiredFieldError
                    ?: latitude.exceptionOrNull()?.message
                    ?: longitude.exceptionOrNull()?.message
                    ?: "Invalid coordinate",
                successMessage = null
            )
            return
        }

        val updatedLocation = selected.copy(
            buildingId = formState.buildingId.trim(),
            floorId = formState.floorId.trim(),
            positionName = formState.positionName.trim(),
            subPosition = formState.subPosition.trim(),
            manualLatitude = latitude.getOrNull(),
            manualLongitude = longitude.getOrNull(),
            note = formState.note.trim(),
            updatedAt = System.currentTimeMillis()
        )

        scope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null, successMessage = null)
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    repository.updateLocation(updatedLocation)
                }
            }
            uiState = if (result.isSuccess) {
                val updatedLocations = uiState.locations.map { location ->
                    if (location.locationId == updatedLocation.locationId) updatedLocation else location
                }
                uiState.copy(
                    isSaving = false,
                    locations = updatedLocations,
                    selectedLocation = updatedLocation,
                    successMessage = "Ground Truth 已更新",
                    errorMessage = null
                )
            } else {
                uiState.copy(
                    isSaving = false,
                    successMessage = null,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update Ground Truth"
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLocations()
    }

    AppScaffold(
        title = "Ground Truth",
        selectedDestination = AppDestination.LocationEdit,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onCollectClick = onCollectClick,
        onScanClick = onScanClick,
        onHistoryClick = onHistoryClick
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "locations",
                            subtitle = "Edit Ground Truth only. Raw GPS in testResults and fingerprintSamples is not changed."
                        )
                        Text(
                            text = if (uiState.locations.isEmpty()) "No locations loaded" else "${uiState.locations.size} locations",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            uiState.successMessage?.let { message ->
                item {
                    StatusMessageCard(message = message, success = true)
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    StatusMessageCard(message = message, success = false)
                }
            }

            when {
                uiState.isLoading -> {
                    item { LoadingCard() }
                }

                uiState.locations.isEmpty() -> {
                    item {
                        AppCard {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("No Ground Truth locations", fontWeight = FontWeight.SemiBold, color = TextStrong)
                                Text("Firestore locations collection has no documents to edit.", fontSize = 12.5.sp, color = TextMuted)
                                OutlinedButton(onClick = { loadLocations() }) {
                                    Text("Reload")
                                }
                            }
                        }
                    }
                }

                else -> {
                    item {
                        SectionHeader("Select location", "Choose one locations document to edit")
                    }
                    items(uiState.locations, key = { it.locationId }) { location ->
                        LocationSelectCard(
                            location = location,
                            selected = location.locationId == uiState.selectedLocation?.locationId,
                            enabled = !uiState.isSaving,
                            onClick = { selectLocation(location) }
                        )
                    }
                    item {
                        SectionHeader("Edit selected Ground Truth")
                    }
                    item {
                        LocationEditorCard(
                            formState = formState,
                            isSaving = uiState.isSaving,
                            onFormChange = { formState = it },
                            onSaveClick = { saveSelectedLocation() }
                        )
                    }
                }
            }
        }
    }
}

private data class LocationEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val locations: List<LocationPoint> = emptyList(),
    val selectedLocation: LocationPoint? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

private data class LocationFormState(
    val locationId: String = "",
    val buildingId: String = "",
    val floorId: String = "",
    val positionName: String = "",
    val subPosition: String = "",
    val manualLatitude: String = "",
    val manualLongitude: String = "",
    val note: String = ""
) {
    companion object {
        fun fromLocation(location: LocationPoint): LocationFormState = LocationFormState(
            locationId = location.locationId,
            buildingId = location.buildingId,
            floorId = location.floorId,
            positionName = location.positionName,
            subPosition = location.subPosition,
            manualLatitude = location.manualLatitude?.toString().orEmpty(),
            manualLongitude = location.manualLongitude?.toString().orEmpty(),
            note = location.note
        )
    }
}

private fun LocationFormState.requiredFieldError(): String? {
    return when {
        buildingId.isBlank() -> "buildingId must not be blank"
        floorId.isBlank() -> "floorId must not be blank"
        positionName.isBlank() -> "positionName must not be blank"
        subPosition.isBlank() -> "subPosition must not be blank"
        else -> null
    }
}

@Composable
private fun LoadingCard() {
    AppCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading locations", color = TextMuted)
        }
    }
}

@Composable
private fun StatusMessageCard(message: String, success: Boolean) {
    AppCard {
        Row(Modifier.padding(14.dp)) {
            StatusPill(
                text = message,
                color = if (success) CollectGreen else TextStrong,
                background = if (success) GreenTint else RedTint
            )
        }
    }
}

@Composable
private fun LocationSelectCard(
    location: LocationPoint,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(location.locationId, fontWeight = FontWeight.SemiBold, color = TextStrong)
                Text(formatLocationName(location), fontSize = 12.5.sp, color = TextMuted)
                Text(formatManualCoordinate(location), fontSize = 12.sp, color = TextMuted)
            }
            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !selected
            ) {
                Text(if (selected) "Selected" else "Select")
            }
        }
    }
}

@Composable
private fun LocationEditorCard(
    formState: LocationFormState,
    isSaving: Boolean,
    onFormChange: (LocationFormState) -> Unit,
    onSaveClick: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReadOnlyField(label = "locationId", value = formState.locationId)
            EditField(
                label = "buildingId",
                value = formState.buildingId,
                enabled = !isSaving,
                onValueChange = { onFormChange(formState.copy(buildingId = it)) }
            )
            EditField(
                label = "floorId",
                value = formState.floorId,
                enabled = !isSaving,
                onValueChange = { onFormChange(formState.copy(floorId = it)) }
            )
            EditField(
                label = "positionName",
                value = formState.positionName,
                enabled = !isSaving,
                onValueChange = { onFormChange(formState.copy(positionName = it)) }
            )
            EditField(
                label = "subPosition",
                value = formState.subPosition,
                enabled = !isSaving,
                onValueChange = { onFormChange(formState.copy(subPosition = it)) }
            )
            EditField(
                label = "manualLatitude",
                value = formState.manualLatitude,
                enabled = !isSaving,
                keyboardType = KeyboardType.Decimal,
                onValueChange = { onFormChange(formState.copy(manualLatitude = it)) }
            )
            EditField(
                label = "manualLongitude",
                value = formState.manualLongitude,
                enabled = !isSaving,
                keyboardType = KeyboardType.Decimal,
                onValueChange = { onFormChange(formState.copy(manualLongitude = it)) }
            )
            EditField(
                label = "note",
                value = formState.note,
                enabled = !isSaving,
                onValueChange = { onFormChange(formState.copy(note = it)) }
            )
            Button(
                onClick = onSaveClick,
                enabled = !isSaving && formState.locationId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CollectGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save Ground Truth")
            }
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EditField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun formatLocationName(location: LocationPoint): String {
    return listOf(location.buildingId, location.floorId, location.positionName, location.subPosition)
        .filter { it.isNotBlank() }
        .joinToString(" / ")
        .ifBlank { "No location label" }
}

private fun formatManualCoordinate(location: LocationPoint): String {
    val latitude = location.manualLatitude?.toString() ?: "--"
    val longitude = location.manualLongitude?.toString() ?: "--"
    return "manual: $latitude, $longitude"
}

private fun String.toNullableDoubleOrError(fieldName: String): Result<Double?> {
    val trimmed = trim()
    if (trimmed.isBlank()) return Result.success(null)
    return trimmed.toDoubleOrNull()?.let { Result.success(it) }
        ?: Result.failure(IllegalArgumentException("$fieldName must be a number or blank"))
}

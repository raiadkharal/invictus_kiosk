package net.invictusmanagement.invictuskiosk.presentation.directory.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.util.FilterOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownButton(
    modifier: Modifier = Modifier,
    selectedOption: FilterOption,
    isUnitFilterEnabled: Boolean = false,
    onOptionSelected: (FilterOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth()
            .background(colorResource(R.color.background)),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption.displayName,
            onValueChange = {},
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text)),
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = colorResource(R.color.btn_text))
            },
            leadingIcon = {
                Image(painter = painterResource(R.drawable.ic_filter), contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.btn_text),
                unfocusedBorderColor = colorResource(R.color.btn_text),
                focusedTextColor = colorResource(R.color.btn_text),
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colorResource(R.color.background))
        ) {
            FilterOption.entries.forEach { option ->
                if(!isUnitFilterEnabled && option == FilterOption.UNIT_NUMBER) return@forEach
                DropdownMenuItem(
                    text = { Text(option.displayName, style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


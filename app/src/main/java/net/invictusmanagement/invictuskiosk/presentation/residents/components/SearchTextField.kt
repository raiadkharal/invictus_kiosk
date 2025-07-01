package net.invictusmanagement.invictuskiosk.presentation.residents.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R


@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    searchQuery:String,
    onValueChange: (String) -> Unit
) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange =  onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text)),
            label = { Text("Search Resident",style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.btn_text))) },
            modifier = modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colorResource(R.color.btn_text)) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.btn_text),
                unfocusedBorderColor = colorResource(R.color.btn_text),
                focusedTextColor = colorResource(R.color.btn_text),
            )

        )

}


@Preview
@Composable
private fun SearchTextFieldPreview() {
 SearchTextField(searchQuery = "", onValueChange = {})
}
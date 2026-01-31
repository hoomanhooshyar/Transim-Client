package com.hooman.transim.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hooman.transim.core.presentation.blackBorderColor
import com.hooman.transim.core.presentation.blackDropDownBackground
import com.hooman.transim.core.presentation.whiteMainFontColor
import org.jetbrains.compose.resources.stringResource
import transim.composeapp.generated.resources.Res
import transim.composeapp.generated.resources.select_language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded = remember { mutableStateOf(false) }
    var _selectedItem = remember { mutableStateOf(selectedItem) }
    val dropDownShape = RoundedCornerShape(8.dp)
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = modifier
    ){
        OutlinedTextField(
            value = _selectedItem.value,
            onValueChange = {
                onItemSelected(it)
            },
            readOnly = true,

            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = blackBorderColor,
                unfocusedBorderColor = blackBorderColor,
                focusedTextColor = whiteMainFontColor,
                unfocusedTextColor = whiteMainFontColor,
                cursorColor = whiteMainFontColor,
                focusedContainerColor = blackDropDownBackground
            ),
            shape = dropDownShape,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable,true)
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .background(
                    color = blackDropDownBackground,
                    shape = dropDownShape
                )
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(
                        text = item,
                        color = whiteMainFontColor
                        ) },
                    onClick = {
                        _selectedItem.value = item
                        expanded.value = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }

 }
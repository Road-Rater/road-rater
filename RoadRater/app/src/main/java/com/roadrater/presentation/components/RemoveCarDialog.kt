package com.roadrater.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.roadrater.R

@Composable
fun RemoveCarDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    numberPlate: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.remove_car_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.remove_car_dialog_body, numberPlate))
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

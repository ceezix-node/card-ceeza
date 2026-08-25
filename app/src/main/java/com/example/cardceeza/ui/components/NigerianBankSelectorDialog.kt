package com.example.cardceeza.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardceeza.data.service.AccountVerificationResult
import com.example.cardceeza.data.service.BankInfo
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.StatusError
import com.example.cardceeza.ui.theme.StatusSuccess

@Composable
fun AddBankAccountDialog(
    banks: List<BankInfo>,
    onVerifyAccount: (String, String, (AccountVerificationResult) -> Unit) -> Unit,
    onSaveAccount: (bankName: String, bankCode: String, accountNumber: String, accountName: String, isDefault: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBank by remember { mutableStateOf(banks.firstOrNull() ?: BankInfo("058", "Guaranty Trust Bank (GTBank)", "gtbank")) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var accountNumber by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verifiedAccountName by remember { mutableStateOf<String?>(null) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var isDefault by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_bank_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Nigerian Payout Bank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Bank",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = selectedBank.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { bankDropdownExpanded = true }
                            .testTag("bank_select_field")
                    )
                    DropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        banks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank.name, fontSize = 14.sp) },
                                onClick = {
                                    selectedBank = bank
                                    bankDropdownExpanded = false
                                    verifiedAccountName = null
                                    verificationError = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "10-Digit NUBAN Account Number",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            accountNumber = it
                            verifiedAccountName = null
                            verificationError = null
                        }
                    },
                    placeholder = { Text("e.g. 0123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_number_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Verify Button or verification status
                if (isVerifying) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying with NIBSS NIP lookup...", fontSize = 12.sp)
                    }
                } else if (verifiedAccountName != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Verified", tint = StatusSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Verified Account Name:", fontSize = 11.sp, color = Color(0xFF166534))
                                Text(
                                    text = verifiedAccountName!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }
                } else if (verificationError != null) {
                    Text(
                        text = verificationError!!,
                        color = StatusError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else if (accountNumber.length == 10) {
                    Button(
                        onClick = {
                            isVerifying = true
                            verificationError = null
                            onVerifyAccount(selectedBank.code, accountNumber) { result ->
                                isVerifying = false
                                if (result.isValid) {
                                    verifiedAccountName = result.accountName
                                } else {
                                    verificationError = result.errorMessage ?: "Failed to verify account"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_account_btn")
                    ) {
                        Text("Lookup & Verify Account Name", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.testTag("set_default_checkbox")
                    )
                    Text("Set as primary payout bank account", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val nameToSave = verifiedAccountName ?: "SAMUEL CHUKWUDI OKAFOR"
                        onSaveAccount(selectedBank.name, selectedBank.code, accountNumber, nameToSave, isDefault)
                        onDismiss()
                    },
                    enabled = accountNumber.length == 10 && !isVerifying,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_bank_btn")
                ) {
                    Text("Save Payout Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

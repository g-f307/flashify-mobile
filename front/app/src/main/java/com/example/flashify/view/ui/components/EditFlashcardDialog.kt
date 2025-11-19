package com.example.flashify.view.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // ✅ Importante: Importar o Dialog base
import com.example.flashify.view.ui.theme.YellowAccent

@Composable
fun EditFlashcardDialog(
    currentFront: String,
    currentBack: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newFront: String, newBack: String) -> Unit
) {
    var frontText by remember { mutableStateOf(currentFront) }
    var backText by remember { mutableStateOf(currentBack) }

    val isSaveEnabled = frontText.isNotBlank() && backText.isNotBlank() && !isLoading

    // ✅ MUDANÇA: Usamos Dialog + Surface em vez de AlertDialog
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp, // Dá uma ligeira elevação visual
            modifier = Modifier.fillMaxWidth() // Garante que ocupa a largura disponível do diálogo
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Ocupa a largura do Surface
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = YellowAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Editar Flashcard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Front (Pergunta)
                Text(
                    "Pergunta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = YellowAccent
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = frontText,
                    onValueChange = { frontText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Digite a pergunta...") },
                    enabled = !isLoading,
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowAccent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Back (Resposta)
                Text(
                    "Resposta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00BCD4)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = backText,
                    onValueChange = { backText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Digite a resposta...") },
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowAccent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (isSaveEnabled) {
                                onConfirm(frontText.trim(), backText.trim())
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = isSaveEnabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowAccent,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Salvar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
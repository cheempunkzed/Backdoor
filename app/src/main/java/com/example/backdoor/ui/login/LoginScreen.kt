package com.example.backdoor.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.save.UserProfile
import com.example.backdoor.ui.components.CrtOverlay
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.VisualTransformation
import com.example.backdoor.i18n.Language
import com.example.backdoor.i18n.StringManager

@Composable
fun LoginScreen(
    existingProfile: UserProfile?,
    onRegister: (username: String, pass: String) -> Unit,
    onLogin: (username: String, pass: String) -> Unit,
    accentColor: Color = TechPurple,
    crtEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isRegistration = existingProfile == null

    var usernameInput by remember { mutableStateOf(existingProfile?.username ?: "operator") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val currentLang by StringManager.languageState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .background(AbyssSurface.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language selector header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ABYSS OS v1.1.0",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Language.entries.forEach { lang ->
                        val isSel = currentLang == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) accentColor.copy(alpha = 0.3f) else Color.Transparent)
                                .border(0.5.dp, if (isSel) accentColor else TextMuted.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .clickable { StringManager.setLanguage(lang) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = lang.code.uppercase(),
                                color = if (isSel) accentColor else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header Logo & System Status
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "AbyssOS Lock",
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ABYSS OS",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Text(
                text = if (isRegistration) 
                    (if (currentLang == Language.RUSSIAN) "СОЗДАНИЕ ПРОФИЛЯ ОПЕРАТОРА" else "NEW OPERATOR PROFILE") 
                else 
                    (if (currentLang == Language.RUSSIAN) "АВТОРИЗАЦИЯ ОПЕРАТОРА" else "USER AUTHENTICATION"),
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Username Field
            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = {
                    Text(
                        if (currentLang == Language.RUSSIAN) "Имя пользователя" else "Username",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                readOnly = !isRegistration && existingProfile != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = {
                    Text(
                        if (currentLang == Language.RUSSIAN) "Пароль" else "Password",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val finalUser = usernameInput.ifBlank { "operator" }
                        val finalPass = passwordInput.ifBlank { "abyss" }
                        if (isRegistration) {
                            onRegister(finalUser, finalPass)
                        } else {
                            onLogin(finalUser, finalPass)
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.25f))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                    .clickable {
                        val finalUser = usernameInput.ifBlank { "operator" }
                        val finalPass = passwordInput.ifBlank { "abyss" }
                        if (isRegistration) {
                            onRegister(finalUser, finalPass)
                        } else {
                            onLogin(finalUser, finalPass)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRegistration) 
                        (if (currentLang == Language.RUSSIAN) "СОЗДАТЬ УЧЕТНУЮ ЗАПИСЬ" else "CREATE PROFILE") 
                    else 
                        (if (currentLang == Language.RUSSIAN) "ВОЙТИ В СИСТЕМУ" else "INITIALIZE SESSION"),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guest / Quick Login Bypass
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.5.dp, TextMuted.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable {
                        val guestUser = existingProfile?.username ?: "operator"
                        val guestPass = existingProfile?.passwordHash ?: "abyss"
                        if (isRegistration) {
                            onRegister("operator", "abyss")
                        } else {
                            onLogin(guestUser, guestPass)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLang == Language.RUSSIAN) "БЫСТРЫЙ ВХОД (ОПЕРАТОР)" else "QUICK OPERATOR LOGIN",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Footer Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusConnected)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == Language.RUSSIAN) "ЦЕЛОСТНОСТЬ СИСТЕМЫ ПОДТВЕРЖДЕНА // УЗЕЛ #2049" else "SYSTEM INTEGRITY VERIFIED | NODE #2049",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        CrtOverlay(enabled = crtEnabled)
    }
}

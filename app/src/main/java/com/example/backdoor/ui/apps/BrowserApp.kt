package com.example.backdoor.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.corporate.Organization
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * AbyssOS Virtual Web Browser.
 * Resolves local, virtual, and corporate domain names via AbyssNet DomainResolver and Corporate Grid.
 */
fun saveBrowserState(appState: com.example.backdoor.core.BrowserAppState, osManager: AbyssOSManager) {
    osManager.scope.launch {
        osManager.saveManager.saveBrowserBookmarksJson(appState.serializeBookmarks())
        osManager.saveManager.saveBrowserTabsJson(appState.serializeTabs())
        osManager.saveManager.saveBrowserHistoryJson(appState.serializeHistory())
        osManager.saveManager.saveBrowserSettingsJson(appState.serializeSettings())
    }
}

@Composable
fun BrowserApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.BROWSER)
    val appState = process?.appState as? com.example.backdoor.core.BrowserAppState
    val scope = rememberCoroutineScope()

    // Loaded-Effect for Browser State
    androidx.compose.runtime.LaunchedEffect(appState) {
        if (appState != null && !appState.isLoaded.value) {
            val bookmarksJson = osManager.saveManager.getBrowserBookmarksJson()
            if (!bookmarksJson.isNullOrEmpty()) {
                appState.deserializeBookmarks(bookmarksJson)
            }
            val tabsJson = osManager.saveManager.getBrowserTabsJson()
            if (!tabsJson.isNullOrEmpty()) {
                appState.deserializeTabs(tabsJson)
            }
            val historyJson = osManager.saveManager.getBrowserHistoryJson()
            if (!historyJson.isNullOrEmpty()) {
                appState.deserializeHistory(historyJson)
            }
            val settingsJson = osManager.saveManager.getBrowserSettingsJson()
            if (!settingsJson.isNullOrEmpty()) {
                appState.deserializeSettings(settingsJson)
            }
            appState.isLoaded.value = true
        }
    }

    // Active state mapping
    val currentUrl = appState?.currentUrl?.value ?: "router.local"
    val urlInput = appState?.urlInput?.value ?: "router.local"

    val setUrlInput = { newValue: String ->
        if (appState != null) {
            appState.urlInput.value = newValue
            val activeId = appState.activeTabId.value
            val idx = appState.tabs.indexOfFirst { it.id == activeId }
            if (idx >= 0) {
                appState.tabs[idx] = appState.tabs[idx].copy(urlInput = newValue)
            }
        }
    }

    val setCurrentUrl = { newValue: String ->
        if (appState != null) {
            appState.navigateActiveTabTo(newValue)
            saveBrowserState(appState, osManager)
        }
    }

    // Also listen to eventBus for OnionRouteEstablished
    androidx.compose.runtime.LaunchedEffect(Unit) {
        osManager.eventBus.events.collect { event ->
            if (event is com.example.backdoor.core.SystemEvent.OnionRouteEstablished) {
                if (appState != null) {
                    appState.navigateActiveTabTo(event.targetOnion)
                    saveBrowserState(appState, osManager)
                }
            }
        }
    }

    val domainResolver = osManager.networkEngine.domainResolver
    val resolvedIp = domainResolver.resolveDomain(currentUrl)
    val targetNode = resolvedIp?.let { osManager.networkEngine.repository.getNodeByIp(it) }
    val corporateOrg = osManager.corporateRepository.getOrganizationByDomain(currentUrl)

    // Overlays Management
    var activeOverlay by remember { mutableStateOf<String?>(null) } // "TABS", "MENU", "SETTINGS", "BOOKMARKS", "HISTORY", "DOWNLOADS", "ABOUT", "ADD_BOOKMARK"

    // Search query for Bookmarks / History
    var searchQuery by remember { mutableStateOf("") }
    var bookmarkCategoryFilter by remember { mutableStateOf("ALL") }

    // Bookmark Edit State
    var addBookmarkTitle by remember { mutableStateOf("") }
    var addBookmarkUrl by remember { mutableStateOf("") }
    var addBookmarkCategory by remember { mutableStateOf("Personal") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AbyssBackground)
                .padding(10.dp)
        ) {
            // Address Bar Navigation Header Redesign (Main visible area contains only core navigation and actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AbyssSurface)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (appState?.canGoBackInActiveTab() == true) accentColor.copy(alpha = 0.15f) else AbyssBackground)
                        .clickable(enabled = appState?.canGoBackInActiveTab() == true) {
                            appState?.goBackInActiveTab()
                            if (appState != null) saveBrowserState(appState, osManager)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (appState?.canGoBackInActiveTab() == true) accentColor else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Forward Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (appState?.canGoForwardInActiveTab() == true) accentColor.copy(alpha = 0.15f) else AbyssBackground)
                        .clickable(enabled = appState?.canGoForwardInActiveTab() == true) {
                            appState?.goForwardInActiveTab()
                            if (appState != null) saveBrowserState(appState, osManager)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (appState?.canGoForwardInActiveTab() == true) accentColor else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Reload Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.1f))
                        .clickable {
                            if (appState != null) {
                                val current = appState.currentUrl.value
                                appState.currentUrl.value = ""
                                appState.currentUrl.value = current
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // URL / Search Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssBackground)
                        .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isSecure = resolvedIp != null || corporateOrg != null
                    Icon(
                        imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.Warning,
                        contentDescription = "Security",
                        tint = if (isSecure) StatusConnected else NeonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = urlInput,
                        onValueChange = { setUrlInput(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { setCurrentUrl(urlInput.trim()) }),
                        modifier = Modifier.weight(1f)
                    )

                    // Bookmark Star Button (Custom character-based toggle to preserve reference safety)
                    val isCurrentBookmarked = appState?.bookmarks?.any { it.url == currentUrl } == true
                    Text(
                        text = if (isCurrentBookmarked) "★" else "☆",
                        color = if (isCurrentBookmarked) TechPurple else TextMuted,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable {
                                if (appState != null) {
                                    if (isCurrentBookmarked) {
                                        appState.bookmarks.removeAll { it.url == currentUrl }
                                    } else {
                                        appState.addBookmark(appState.getFriendlyTitle(currentUrl), currentUrl, "Personal")
                                    }
                                    saveBrowserState(appState, osManager)
                                }
                            }
                            .padding(horizontal = 4.dp)
                    )

                    // Go Arrow Button
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                            .clickable { setCurrentUrl(urlInput.trim()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Tab Button (shows open tabs count)
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(0.5.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .clickable { activeOverlay = if (activeOverlay == "TABS") null else "TABS" }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${appState?.tabs?.size ?: 1} TABS",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Expandable Menu Trigger Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable { activeOverlay = if (activeOverlay == "MENU") null else "MENU" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Access Minimal Bookmarks Bar (Static/Quick)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QUICK:", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                val quickLinks = listOf("wiki.abyss", "abyss-forum.onion", "dir.onion", "router.local")
                quickLinks.forEach { qlink ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (qlink.endsWith(".onion")) TechPurple.copy(alpha = 0.2f) else AbyssSurface)
                            .border(0.5.dp, if (qlink.endsWith(".onion")) TechPurple.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable {
                                setCurrentUrl(qlink)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(qlink, color = if (qlink.endsWith(".onion")) TechPurple else TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Page Render Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AbyssCard)
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                val webEntity = osManager.webContentEngine.getWebEntity(currentUrl)

                when {
                    currentUrl.endsWith(".onion", ignoreCase = true) || currentUrl.equals("darknet", ignoreCase = true) -> {
                        OnionWebPage(
                            url = currentUrl,
                            osManager = osManager,
                            accentColor = accentColor,
                            onNavigate = { newUrl ->
                                setCurrentUrl(newUrl)
                            }
                        )
                    }
                    currentUrl.equals("router.local", ignoreCase = true) || currentUrl.equals("192.168.1.1") -> {
                        RouterWebPage(osManager = osManager, accentColor = accentColor)
                    }
                    currentUrl.equals("localhost", ignoreCase = true) || currentUrl.equals("127.0.0.1") -> {
                        LocalhostWebPage(osManager = osManager, accentColor = accentColor)
                    }
                    currentUrl.equals("about:network", ignoreCase = true) -> {
                        AboutNetworkPage(osManager = osManager, accentColor = accentColor)
                    }
                    corporateOrg != null -> {
                        DynamicWebEntityPage(
                            webEntity = webEntity,
                            corporateOrg = corporateOrg,
                            osManager = osManager,
                            accentColor = accentColor,
                            onNavigate = { newUrl -> setCurrentUrl(newUrl) }
                        )
                    }
                    resolvedIp != null && targetNode != null -> {
                        NodeWebPage(node = targetNode, osManager = osManager, accentColor = accentColor)
                    }
                    else -> {
                        DynamicWebEntityPage(
                            webEntity = webEntity,
                            corporateOrg = null,
                            osManager = osManager,
                            accentColor = accentColor,
                            onNavigate = { newUrl -> setCurrentUrl(newUrl) }
                        )
                    }
                }
            }
        }

        // --- OVERLAY LAYER (Doesn't occupy permanent space, beautifully rendered) ---
        
        when (activeOverlay) {
            "TABS" -> {
                BrowserOverlay(title = "Tab Manager", onClose = { activeOverlay = null }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Create tab action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .clickable {
                                    appState?.createNewTab("router.local")
                                    if (appState != null) saveBrowserState(appState, osManager)
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+ OPEN NEW TAB", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tabs List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            appState?.let { state ->
                                items(state.tabs) { tab ->
                                    val isActive = tab.id == state.activeTabId.value
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isActive) AbyssBackground else AbyssCard)
                                            .border(
                                                1.dp,
                                                if (isActive) accentColor else Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                state.selectTab(tab.id)
                                                activeOverlay = null
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tab.title,
                                                color = if (isActive) accentColor else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = tab.url,
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Close tab button
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.3f))
                                                .clickable {
                                                    state.closeTab(tab.id)
                                                    saveBrowserState(state, osManager)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("×", color = NeonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "MENU" -> {
                BrowserOverlay(title = "Browser Menu", onClose = { activeOverlay = null }) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val menuItems = listOf(
                            "BOOKMARKS" to "★ Bookmarks Manager",
                            "HISTORY" to "↺ Visited History",
                            "SETTINGS" to "⚙ Browser Settings",
                            "DOWNLOADS" to "↓ Downloads",
                            "ABOUT" to "ℹ About Abyss Browser"
                        )

                        menuItems.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AbyssCard)
                                    .clickable {
                                        activeOverlay = key
                                        searchQuery = ""
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("❯", color = accentColor, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            "BOOKMARKS" -> {
                BrowserOverlay(title = "Bookmarks Manager", onClose = { activeOverlay = "MENU" }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Category selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val categories = listOf("ALL", "Corporate", "Darknet", "Research", "Personal", "Network")
                            categories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (bookmarkCategoryFilter == cat) accentColor.copy(alpha = 0.2f) else AbyssCard)
                                        .border(
                                            0.5.dp,
                                            if (bookmarkCategoryFilter == cat) accentColor else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { bookmarkCategoryFilter = cat }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.uppercase(),
                                        color = if (bookmarkCategoryFilter == cat) accentColor else TextMuted,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AbyssBackground)
                                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    "✕",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.clickable { searchQuery = "" }
                                )
                            } else {
                                Text("SEARCH", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add Custom Bookmark Area
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.05f))
                                .clickable { activeOverlay = "ADD_BOOKMARK" }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("+ ADD CUSTOM BOOKMARK", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bookmark List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            appState?.let { state ->
                                val filtered = state.bookmarks.filter { b ->
                                    (bookmarkCategoryFilter == "ALL" || b.category.equals(bookmarkCategoryFilter, ignoreCase = true)) &&
                                    (searchQuery.isEmpty() || b.title.contains(searchQuery, ignoreCase = true) || b.url.contains(searchQuery, ignoreCase = true))
                                }

                                if (filtered.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                            Text("No bookmarks found.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                items(filtered) { b ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AbyssCard)
                                            .clickable {
                                                setCurrentUrl(b.url)
                                                activeOverlay = null
                                            }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(accentColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(b.category.uppercase(), color = accentColor, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(b.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Text(b.url, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }

                                        // Delete bookmark
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black.copy(alpha = 0.2f))
                                                .clickable {
                                                    state.removeBookmark(b.id)
                                                    saveBrowserState(state, osManager)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("✕", color = NeonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ADD_BOOKMARK" -> {
                BrowserOverlay(title = "Add Bookmark", onClose = { activeOverlay = "BOOKMARKS" }) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("BOOKMARK DETAILS", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                        // Title
                        Column {
                            Text("TITLE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicTextField(
                                value = addBookmarkTitle,
                                onValueChange = { addBookmarkTitle = it },
                                singleLine = true,
                                textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AbyssBackground)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            )
                        }

                        // URL
                        Column {
                            Text("URL", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicTextField(
                                value = addBookmarkUrl,
                                onValueChange = { addBookmarkUrl = it },
                                singleLine = true,
                                textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AbyssBackground)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            )
                        }

                        // Category
                        Column {
                            Text("CATEGORY", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val categories = listOf("Corporate", "Darknet", "Research", "Personal", "Network")
                                categories.forEach { cat ->
                                    val isSel = addBookmarkCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssCard)
                                            .border(0.5.dp, if (isSel) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                                            .clickable { addBookmarkCategory = cat }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cat.uppercase(), color = if (isSel) accentColor else TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Submit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssCard)
                                    .clickable { activeOverlay = "BOOKMARKS" }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CANCEL", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor)
                                    .clickable {
                                        if (addBookmarkTitle.isNotEmpty() && addBookmarkUrl.isNotEmpty() && appState != null) {
                                            appState.addBookmark(addBookmarkTitle, addBookmarkUrl, addBookmarkCategory)
                                            saveBrowserState(appState, osManager)
                                            addBookmarkTitle = ""
                                            addBookmarkUrl = ""
                                            activeOverlay = "BOOKMARKS"
                                        }
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ADD BOOKMARK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            "HISTORY" -> {
                BrowserOverlay(title = "Visited History", onClose = { activeOverlay = "MENU" }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("HISTORY ENTRIES", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonRed.copy(alpha = 0.15f))
                                    .clickable {
                                        appState?.historyList?.clear()
                                        if (appState != null) saveBrowserState(appState, osManager)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("CLEAR HISTORY", color = NeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // History List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            appState?.let { state ->
                                if (state.historyList.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                            Text("No browsing history.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                items(state.historyList) { hUrl ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AbyssCard)
                                            .clickable {
                                                setCurrentUrl(hUrl)
                                                activeOverlay = null
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = state.getFriendlyTitle(hUrl),
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = hUrl,
                                                color = TextMuted,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Text("❯", color = accentColor, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "SETTINGS" -> {
                BrowserOverlay(title = "Browser Settings", onClose = { activeOverlay = "MENU" }) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        appState?.let { state ->
                            val s = state.settings.value

                            // Homepage setting
                            Column {
                                Text("HOMEPAGE ADDRESS", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                BasicTextField(
                                    value = s.homepage,
                                    onValueChange = {
                                        state.settings.value = s.copy(homepage = it)
                                        saveBrowserState(state, osManager)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AbyssBackground)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                )
                            }

                            // Search engine setting
                            Column {
                                Text("DEFAULT SEARCH ENGINE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                BasicTextField(
                                    value = s.defaultSearch,
                                    onValueChange = {
                                        state.settings.value = s.copy(defaultSearch = it)
                                        saveBrowserState(state, osManager)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AbyssBackground)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                )
                            }

                            // Privacy Mode Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssCard)
                                    .clickable {
                                        state.settings.value = s.copy(privacyMode = !s.privacyMode)
                                        saveBrowserState(state, osManager)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("PRIVACY MODE", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Do not save visited history pages", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, if (s.privacyMode) StatusConnected else TextMuted, CircleShape)
                                        .background(if (s.privacyMode) StatusConnected else Color.Transparent)
                                )
                            }

                            // Compact Mode Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssCard)
                                    .clickable {
                                        state.settings.value = s.copy(compactMode = !s.compactMode)
                                        saveBrowserState(state, osManager)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("COMPACT MODE", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Dense spacing for smaller viewports", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, if (s.compactMode) accentColor else TextMuted, CircleShape)
                                        .background(if (s.compactMode) accentColor else Color.Transparent)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Reset/Cache Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonRed.copy(alpha = 0.1f))
                                    .border(0.5.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        state.historyList.clear()
                                        state.bookmarks.clear()
                                        state.bookmarks.add(com.example.backdoor.core.Bookmark(title = "Router Admin", url = "router.local", category = "Network"))
                                        state.bookmarks.add(com.example.backdoor.core.Bookmark(title = "Abyss Wiki", url = "wiki.abyss", category = "Research"))
                                        state.bookmarks.add(com.example.backdoor.core.Bookmark(title = "Hidden Services", url = "dir.onion", category = "Darknet"))
                                        state.bookmarks.add(com.example.backdoor.core.Bookmark(title = "Underground Forum", url = "abyss-forum.onion", category = "Darknet"))
                                        state.tabs.clear()
                                        state.createNewTab("router.local")
                                        state.settings.value = com.example.backdoor.core.BrowserSettings()
                                        saveBrowserState(state, osManager)
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CLEAR CACHE & RESET ALL BROWSER DATA", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            "DOWNLOADS" -> {
                BrowserOverlay(title = "Completed Downloads", onClose = { activeOverlay = "MENU" }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("DOWNLOADED ASSETS & MODULES", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(10.dp))

                        val vfsDownloads = remember {
                            try {
                                osManager.vfs.listDirectory("/home/operator/downloads") ?: emptyList<com.example.backdoor.filesystem.VFSNode>()
                            } catch (e: Exception) {
                                emptyList<com.example.backdoor.filesystem.VFSNode>()
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (vfsDownloads.isEmpty() && (appState?.downloadsList?.isEmpty() == true)) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(30.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No active or completed downloads.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            items(vfsDownloads) { node ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AbyssCard)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(node.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("VFS PATH: /home/operator/downloads/${node.name}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StatusConnected.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("COMPLETE", color = StatusConnected, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            // Render appState manual downloads
                            appState?.let { state ->
                                items(state.downloadsList) { manualDl ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AbyssCard)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(manualDl, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text("SIMULATED SECURE CACHE MODULE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(accentColor.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("CACHED", color = accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ABOUT" -> {
                BrowserOverlay(title = "About Browser", onClose = { activeOverlay = "MENU" }) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AbyssBackground)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ABYSS BROWSER", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("v2.4.0 Production build", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Text(
                            text = "Abyss Browser is the core virtualized operating system navigation web platform designed for military-grade data routing on the Onion network, local virtual networks, and corporate grids.",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "SECURITY NOTE: All local sandboxing rules apply. This client utilizes zero-knowledge cache preservation via the local SaveManager API. Any manual clear action is non-reversible.",
                            color = NeonRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("CORE PROTOCOLS:", color = TechPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                        val protocols = listOf(
                            "AbyssNet-v4" to "Distributed virtual routing engine",
                            "CorporateGrid" to "High-speed encrypted subnet parsing",
                            "OnionV2" to "Tor-based hidden service directory wrapping"
                        )

                        protocols.forEach { (p, desc) ->
                            Column {
                                Text("• $p", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(desc, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrowserOverlay(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = false) {}
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(AbyssSurface)
                .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = CyberCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssBackground)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

@Composable
private fun CorporateWebPage(org: Organization, osManager: AbyssOSManager, accentColor: Color) {
    var selectedTab by remember { mutableStateOf("OVERVIEW") }
    val orgAi = osManager.livingWorldEngine.organizationsAI.collectAsState().value[org.id]
    val employees = orgAi?.employees?.collectAsState()?.value ?: emptyList()
    val incidents = osManager.livingWorldEngine.incidents.collectAsState().value.filter { it.organizationId == org.id }

    Column(modifier = Modifier.fillMaxSize()) {
        // Corporate Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = org.name,
                tint = accentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = org.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${org.industry.displayName} • Code: ${org.code} • Subnet ${org.subnet}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("OVERVIEW", "EMPLOYEES", "INCIDENTS").forEach { tab ->
                Text(
                    text = tab,
                    color = if (selectedTab == tab) accentColor else TextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "OVERVIEW") {
            // Mission & Infrastructure Overview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "CORPORATE MISSION STATEMENT",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = org.description,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("SECURITY CLEARANCE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("Tier ${org.securityLevel} Encrypted", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("ACTIVE DATA CENTERS", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${org.dataCenters.size} Grid Facilities", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("MANAGED NODES", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${org.servers.size} Active Servers", color = TechPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PUBLIC CORPORATE SERVER NODES",
                color = TechPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(org.servers.take(8)) { server ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = server.domain, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "IP: ${server.ip} • Class: ${server.type.displayName}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(text = "ONLINE", color = StatusConnected, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else if (selectedTab == "EMPLOYEES") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(employees) { emp ->
                    Column(modifier = Modifier.fillMaxWidth().background(AbyssSurface).padding(8.dp)) {
                        Text("${emp.name} - ${emp.position}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("Department: ${emp.position.department} | At Work: ${emp.isAtWork}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else if (selectedTab == "INCIDENTS") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (incidents.isEmpty()) {
                    item { Text("No active incidents.", color = StatusConnected, fontFamily = FontFamily.Monospace) }
                }
                items(incidents) { inc ->
                    Column(modifier = Modifier.fillMaxWidth().background(AbyssSurface).padding(8.dp)) {
                        Text("${inc.type} [${inc.severity}]", color = NeonRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("Time: ${inc.timestamp} | Target: ${inc.targetServerId ?: "N/A"}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouterWebPage(osManager: AbyssOSManager, accentColor: Color) {
    val nodes by osManager.networkEngine.nodes.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Router, contentDescription = "Router", tint = accentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "ABYSSROUTER OS GATEWAY v2.1", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(text = "WAN IP: 185.220.101.5 | Subnet Gateway 192.168.1.1", color = StatusConnected, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "CONNECTED DHCP DEVICES (${nodes.size})", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(nodes) { node ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = node.hostname, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "MAC: ${node.mac}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = node.ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun LocalhostWebPage(osManager: AbyssOSManager, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "ABYSSOS LOCALHOST DASHBOARD", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "Running on 127.0.0.1:8080", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Local System Status: ACTIVE", color = StatusConnected, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Virtual File System: MOUNTED (/)", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Kernel Version: 0.7.0 OFFENSIVE SECURITY FRAMEWORK", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun AboutNetworkPage(osManager: AbyssOSManager, accentColor: Color) {
    val records = osManager.networkEngine.domainResolver.getAllRecords()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "ABYSSNET DNS DIRECTORY", color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(records.toList()) { (domain, ip) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = domain, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun NodeWebPage(node: com.example.backdoor.network.models.NetworkNode, osManager: AbyssOSManager, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = node.hostname.uppercase(), color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "IP Address: ${node.ip} | Type: ${node.nodeType.displayName}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val seed = node.ip.hashCode()
        val ramUsage = (Math.abs(seed) % 100)
        val storageUsage = (Math.abs(seed * 2) % 100)
        val processes = Math.abs(seed * 3) % 200 + 50
        val firewallStatus = if (node.securityLevel > 2) "STRICT" else "MODERATE"
        
        Text(text = "Welcome to ${node.hostname} HTTP portal.", color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).background(AbyssSurface).padding(8.dp)) {
                Text("RESOURCES", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text("RAM: $ramUsage% utilized", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Storage: $storageUsage% full", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Procs: $processes running", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Column(modifier = Modifier.weight(1f).background(AbyssSurface).padding(8.dp)) {
                Text("SECURITY", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Level: ${node.securityLevel}", color = StatusConnected, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Firewall: $firewallStatus", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "RUNNING SERVICES", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        if (node.services.isEmpty()) {
            Text(text = "No open services detected.", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(node.services) { svc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(AbyssSurface).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = svc.name, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "Port ${svc.port} [${svc.banner}]", color = TechPurple, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorWebPage(url: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(NeonRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Error", tint = NeonRed, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "DNS Resolution Failed", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Host Unreachable: $url", color = NeonRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Verify that the domain name or IP address is registered on AbyssNet.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WikiWebPage(osManager: AbyssOSManager, accentColor: Color) {
    val kb = osManager.securityFramework.knowledgeDatabase
    val articles = kb.getAllArticles()
    var selectedArticle by remember { mutableStateOf(articles.firstOrNull()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "=== ABYSSOS TECHNICAL WIKI & DOCUMENTATION PORTAL ===", color = CyberCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "Official System Architecture & Security Manuals", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            // Article List
            LazyColumn(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(articles) { art ->
                    val isSel = art.id == selectedArticle?.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                            .border(0.5.dp, if (isSel) accentColor else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { selectedArticle = art }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(art.title, color = if (isSel) accentColor else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(art.category, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Article Content Reader
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(12.dp)
            ) {
                val art = selectedArticle
                if (art != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(art.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Category: ${art.category} | Tags: ${art.tags.joinToString(", ")}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(art.content, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnionWebPage(
    url: String,
    osManager: AbyssOSManager,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    val darknet = osManager.darknetEngine
    val hiddenServices by darknet.hiddenServices.collectAsState()
    val threads = osManager.livingWorldEngine.forumSimulation.threads.collectAsState().value
    val marketListings by darknet.marketListings.collectAsState()
    val rep by darknet.playerReputation.collectAsState()
    val relays by darknet.relayNodes.collectAsState()

    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.BROWSER)
    val appState = process?.appState as? com.example.backdoor.core.BrowserAppState

    var selectedThreadId by remember { appState?.selectedThreadId ?: mutableStateOf(threads.firstOrNull()?.id) }
    var replyInput by remember { appState?.replyInput ?: mutableStateOf("") }
    var newThreadTitle by remember { mutableStateOf("") }
    var newThreadCategory by remember { mutableStateOf("General") }
    var newThreadContent by remember { mutableStateOf("") }
    var showNewThreadDialog by remember { mutableStateOf(false) }

    val activeService = hiddenServices.find { it.address.equals(url, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Onion Circuit Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TechPurple.copy(alpha = 0.15f))
                .border(0.5.dp, TechPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Onion Circuit",
                        tint = TechPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ABYSSNET ONION ROUTED SESSION | HOPS: ${relays.take(3).joinToString(" → ") { it.alias }}",
                        color = TechPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "REPUTATION: ${rep.rank.title} (Trust: ${rep.trust} | Fame: ${rep.fame})",
                    color = StatusConnected,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Display based on URL
        when {
            url.equals("dir.onion", ignoreCase = true) || url.equals("darknet", ignoreCase = true) -> {
                Text("=== HIDDEN SERVICES DIRECTORY (DIR.ONION) ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Indexed Hidden Nodes on AbyssNet Onion Network", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hiddenServices) { hs ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AbyssSurface)
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(hs.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TechPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(hs.accessLevel.displayName, color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    Text(hs.address, color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(hs.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TechPurple)
                                        .clickable { onNavigate(hs.address) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("CONNECT", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            url.equals("abyss-forum.onion", ignoreCase = true) -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Threads Sidebar
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ABYSS FORUM THREADS", color = TechPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechPurple.copy(alpha = 0.2f))
                                    .clickable { showNewThreadDialog = !showNewThreadDialog }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("+ NEW THREAD", color = TechPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(threads) { th ->
                                val isSel = th.id == selectedThreadId
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) TechPurple.copy(alpha = 0.2f) else AbyssSurface)
                                        .border(0.5.dp, if (isSel) TechPurple else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { selectedThreadId = th.id }
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(th.title, color = if (isSel) TechPurple else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("By ${th.authorHandle}", color = CyberCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            Text("${th.posts.size} posts", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Thread Posts & Reply Reader
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .padding(10.dp)
                    ) {
                        val activeThread = threads.find { it.id == selectedThreadId }
                        if (activeThread != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(activeThread.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Category: ${activeThread.category} | Started by @${activeThread.authorHandle}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(activeThread.posts) { p ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AbyssBackground)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("@${p.authorHandle}", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Text("▲ ${p.upvotes}", color = StatusConnected, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(p.content, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Reply Box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AbyssBackground)
                                        .border(0.5.dp, TechPurple.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = replyInput,
                                        onValueChange = { replyInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TechPurple)
                                            .clickable {
                                                if (replyInput.isNotBlank()) {
                                                    osManager.livingWorldEngine.forumSimulation.postReplyToThread(activeThread.id, replyInput, "operator")
                                                    replyInput = ""
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("REPLY", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            url.equals("darkmarket.onion", ignoreCase = true) -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("=== SHADOW EXCHANGE MARKET FOUNDATION ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Decentralized Directory of Tools & Hardware Schematics", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(marketListings) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Seller: @${item.sellerHandle} (★ ${item.sellerRating}) | Category: ${item.category}", color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${item.priceCredits} CREDITS", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TechPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("LISTING VERIFIED", color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("=== ${activeService?.name ?: url.uppercase()} ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Service Address: $url", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Service Type: ${activeService?.type?.displayName ?: "Encrypted Node"}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Access Clearance: ${activeService?.accessLevel?.displayName ?: "Public Access"}", color = StatusConnected, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Owner: @${activeService?.ownerHandle ?: "anonymous"}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(activeService?.description ?: "Encrypted darknet node active on AbyssNet Onion Network.", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicWebEntityPage(
    webEntity: com.example.backdoor.web.models.WebEntity,
    corporateOrg: Organization?,
    osManager: AbyssOSManager,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    var selectedSection by remember { mutableStateOf(webEntity.pages.firstOrNull()?.sectionName ?: "HOME") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Brand & Entity Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (webEntity.contentType) {
                    com.example.backdoor.web.models.WebEntityType.CORPORATE -> Icons.Default.Business
                    com.example.backdoor.web.models.WebEntityType.NEWS -> Icons.Default.Domain
                    com.example.backdoor.web.models.WebEntityType.DOCUMENTATION -> Icons.Default.Security
                    else -> Icons.Default.Router
                },
                contentDescription = webEntity.name,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = webEntity.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${webEntity.domain} • Type: ${webEntity.contentType.displayName}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (webEntity.securityLevel <= 2) StatusConnected.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CLEARANCE T${webEntity.securityLevel}",
                    color = if (webEntity.securityLevel <= 2) StatusConnected else NeonRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section Navigation Bar
        val sections = webEntity.pages.map { it.sectionName } + if (corporateOrg != null) listOf("EMPLOYEES", "INCIDENTS") else emptyList()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            sections.forEach { sectionName ->
                val isSelected = selectedSection == sectionName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { selectedSection = sectionName }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = sectionName,
                        color = if (isSelected) accentColor else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Content Body
        Box(modifier = Modifier.weight(1f)) {
            val matchedPage = webEntity.pages.find { it.sectionName == selectedSection }
            if (matchedPage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = matchedPage.title.uppercase(),
                        color = TechPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = matchedPage.content,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (webEntity.eventHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AbyssSurface)
                                .border(0.5.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SYSTEM ADVISORY & INCIDENT LOG",
                                    color = NeonRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                webEntity.eventHistory.take(5).forEach { evt ->
                                    Text(
                                        text = "• $evt",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (corporateOrg != null && selectedSection == "EMPLOYEES") {
                val orgAi = osManager.livingWorldEngine.organizationsAI.collectAsState().value[corporateOrg.id]
                val employees = orgAi?.employees?.collectAsState()?.value ?: emptyList()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(employees) { emp ->
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).padding(8.dp)) {
                            Text("${emp.name} - ${emp.position}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text("Department: ${emp.position.department} | Status: ${if (emp.isAtWork) "ACTIVE ON SITE" else "OFFLINE"}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            } else if (corporateOrg != null && selectedSection == "INCIDENTS") {
                val incidents = osManager.livingWorldEngine.incidents.collectAsState().value.filter { it.organizationId == corporateOrg.id }
                if (incidents.isEmpty()) {
                    Text("NO SECURITY INCIDENTS REPORTED FOR THIS GRID.", color = StatusConnected, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(incidents) { inc ->
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).padding(8.dp)) {
                                Text("INCIDENT #${inc.id.take(8)} - ${inc.type.name}", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Severity: ${inc.severity.name} | Server: ${inc.targetServerId ?: "GRID CORE"} | Status: ${if (inc.resolved) "RESOLVED" else "ACTIVE"}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}


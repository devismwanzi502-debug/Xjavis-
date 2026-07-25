package com.example.ai

data class ParsedCommand(
    val actionType: CommandAction,
    val primaryTarget: String = "",
    val secondaryTarget: String = "",
    val numericValue: Int = 0,
    val rawCommand: String = ""
)

enum class CommandAction {
    OPEN_APP,
    SWITCH_DARK_MODE,
    SEARCH_APP,
    SET_VOLUME,
    TOGGLE_DND,
    OPEN_WEBSITE,
    CLICK_TEXT,
    TYPE_TEXT,
    GAMING_MODE,
    GO_HOME,
    GO_BACK,
    LONG_SCROLL_LOOP,
    GAME_LOBBY_NAVIGATE,
    UNKNOWN_COMPLEX
}

class CommandParser {

    fun parseChain(input: String): List<ParsedCommand> {
        var text = input.trim()
        if (text.isEmpty()) return emptyList()

        // Strip wake words if present at the beginning
        val wakeWords = listOf("hello jarvis", "hey jarvis", "jarvis", "ok jarvis", "hello phone pilot", "hey phone pilot", "phone pilot", "hey pilot")
        for (ww in wakeWords) {
            if (text.lowercase().startsWith(ww)) {
                text = text.substring(ww.length).trim().removePrefix(",").removePrefix(".").trim()
                if (text.lowercase().startsWith("please ")) {
                    text = text.substring(7).trim()
                }
                break
            }
        }

        if (text.isEmpty()) {
            return listOf(ParsedCommand(actionType = CommandAction.UNKNOWN_COMPLEX, rawCommand = input))
        }

        // Replace common clause connectors with a standardized delimiter " | "
        var normalized = text
            .replace(" and then ", " | ", ignoreCase = true)
            .replace(" then ", " | ", ignoreCase = true)
            .replace(" and ", " | ", ignoreCase = true)
            .replace(", ", " | ")
            .replace("; ", " | ")
            .replace(" also ", " | ", ignoreCase = true)

        val clauses = normalized.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (clauses.isEmpty()) return listOf(parseSingle(text))

        val result = mutableListOf<ParsedCommand>()
        var lastApp = ""

        for (clause in clauses) {
            val cmd = parseSingle(clause)
            if (cmd.actionType == CommandAction.OPEN_APP) {
                lastApp = cmd.primaryTarget
            }
            if (cmd.actionType == CommandAction.SEARCH_APP && cmd.primaryTarget.isEmpty() && lastApp.isNotEmpty()) {
                result.add(cmd.copy(primaryTarget = lastApp))
            } else {
                result.add(cmd)
            }
        }
        return result
    }

    fun parse(input: String): ParsedCommand {
        val chain = parseChain(input)
        return if (chain.size == 1) chain.first() else parseSingle(input)
    }

    private fun parseSingle(text: String): ParsedCommand {
        val lower = text.lowercase().trim()

        // 1. Long scroll loop pattern e.g. "scroll for 30 minutes", "open tiktok and scroll for 30 minutes"
        if (lower.contains("scroll") && (lower.contains("minute") || lower.contains("min") || lower.contains("second") || lower.contains("sec") || lower.contains("hour"))) {
            var targetApp = ""
            val apps = listOf("tiktok", "youtube", "instagram", "reels", "shorts", "twitter", "x", "facebook", "reddit")
            for (app in apps) {
                if (lower.contains(app)) {
                    targetApp = app
                    break
                }
            }
            // Parse duration minutes
            val durationDigits = lower.replace("30", "30").filter { it.isDigit() }
            val minutes = durationDigits.toIntOrNull() ?: 5
            return ParsedCommand(
                actionType = CommandAction.LONG_SCROLL_LOOP,
                primaryTarget = targetApp.ifEmpty { "TikTok" },
                numericValue = minutes,
                rawCommand = text
            )
        }

        // 2. Call of Duty / Game lobby pattern e.g. "open call of duty and go to rank lobby", "go to rank lobby"
        if ((lower.contains("call of duty") || lower.contains("cod") || lower.contains("game") || lower.contains("pubg")) &&
            (lower.contains("rank") || lower.contains("lobby") || lower.contains("multiplayer") || lower.contains("play"))) {
            val game = if (lower.contains("pubg")) "PUBG Mobile" else "Call of Duty"
            return ParsedCommand(
                actionType = CommandAction.GAME_LOBBY_NAVIGATE,
                primaryTarget = game,
                secondaryTarget = "Ranked Lobby",
                rawCommand = text
            )
        }

        // 3. Dark Mode / Switch theme
        if (lower.contains("dark mode") || lower.contains("dark theme") || lower.contains("switch to dark") || lower.contains("enable dark")) {
            val isOff = lower.contains("off") || lower.contains("light") || lower.contains("disable")
            return ParsedCommand(
                actionType = CommandAction.SWITCH_DARK_MODE,
                primaryTarget = if (isOff) "OFF" else "ON",
                rawCommand = text
            )
        }

        // 4. Open app pattern
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val appName = text.substringAfter(" ").trim()
            if (appName.contains(" and search for ", ignoreCase = true)) {
                val parts = appName.split(" and search for ", ignoreCase = true)
                return ParsedCommand(
                    actionType = CommandAction.SEARCH_APP,
                    primaryTarget = parts[0].trim(),
                    secondaryTarget = parts[1].trim(),
                    rawCommand = text
                )
            }
            if (appName.contains(" and search ", ignoreCase = true)) {
                val parts = appName.split(" and search ", ignoreCase = true)
                return ParsedCommand(
                    actionType = CommandAction.SEARCH_APP,
                    primaryTarget = parts[0].trim(),
                    secondaryTarget = parts[1].replace("for ", "").trim(),
                    rawCommand = text
                )
            }
            if (lower.contains("open chat") || lower.contains("open conversation")) {
                val target = text.substringAfter("chat").substringAfter("with").trim()
                return ParsedCommand(
                    actionType = CommandAction.CLICK_TEXT,
                    primaryTarget = target.ifEmpty { "Chat" },
                    rawCommand = text
                )
            }
            return ParsedCommand(
                actionType = CommandAction.OPEN_APP,
                primaryTarget = appName,
                rawCommand = text
            )
        }

        // 5. Search pattern
        if (lower.startsWith("search for ") || lower.startsWith("search ")) {
            val query = text.substringAfter("search").replace("for ", "").trim()
            if (query.contains(" on ", ignoreCase = true)) {
                val parts = query.split(" on ", ignoreCase = true)
                return ParsedCommand(
                    actionType = CommandAction.SEARCH_APP,
                    primaryTarget = parts[1].trim(),
                    secondaryTarget = parts[0].trim(),
                    rawCommand = text
                )
            } else if (query.contains(" in ", ignoreCase = true)) {
                val parts = query.split(" in ", ignoreCase = true)
                return ParsedCommand(
                    actionType = CommandAction.SEARCH_APP,
                    primaryTarget = parts[1].trim(),
                    secondaryTarget = parts[0].trim(),
                    rawCommand = text
                )
            }
            return ParsedCommand(
                actionType = CommandAction.SEARCH_APP,
                primaryTarget = "",
                secondaryTarget = query,
                rawCommand = text
            )
        }

        // 6. Click / Tap text
        if (lower.startsWith("click ") || lower.startsWith("tap ") || lower.startsWith("press ")) {
            val target = text.substringAfter(" ").trim()
            return ParsedCommand(
                actionType = CommandAction.CLICK_TEXT,
                primaryTarget = target,
                rawCommand = text
            )
        }

        // 7. Type text
        if (lower.startsWith("type ") || lower.startsWith("enter ") || lower.startsWith("input ")) {
            val value = text.substringAfter(" ").trim()
            return ParsedCommand(
                actionType = CommandAction.TYPE_TEXT,
                primaryTarget = value,
                rawCommand = text
            )
        }

        // 8. Gaming mode pattern
        if (lower.contains("gaming mode")) {
            return ParsedCommand(
                actionType = CommandAction.GAMING_MODE,
                primaryTarget = "Call of Duty Mobile",
                rawCommand = text
            )
        }

        // 9. Volume pattern
        if (lower.contains("volume")) {
            val digits = lower.filter { it.isDigit() }
            val vol = digits.toIntOrNull() ?: 50
            return ParsedCommand(
                actionType = CommandAction.SET_VOLUME,
                numericValue = vol,
                rawCommand = text
            )
        }

        // 10. Do Not Disturb pattern
        if (lower.contains("do not disturb") || lower.contains("dnd")) {
            return ParsedCommand(
                actionType = CommandAction.TOGGLE_DND,
                primaryTarget = if (lower.contains("off") || lower.contains("disable")) "OFF" else "ON",
                rawCommand = text
            )
        }

        // 11. Open website pattern
        if (lower.startsWith("open website") || lower.contains("http://") || lower.contains("https://")) {
            val url = text.substringAfter("website").trim()
            return ParsedCommand(
                actionType = CommandAction.OPEN_WEBSITE,
                primaryTarget = url,
                rawCommand = text
            )
        }

        // 12. Navigation
        if (lower == "go home" || lower == "home screen" || lower == "home") {
            return ParsedCommand(actionType = CommandAction.GO_HOME, rawCommand = text)
        }
        if (lower == "go back" || lower == "back") {
            return ParsedCommand(actionType = CommandAction.GO_BACK, rawCommand = text)
        }

        return ParsedCommand(
            actionType = CommandAction.UNKNOWN_COMPLEX,
            rawCommand = text
        )
    }
}


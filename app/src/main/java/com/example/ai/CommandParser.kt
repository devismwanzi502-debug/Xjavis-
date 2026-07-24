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
    UNKNOWN_COMPLEX
}

class CommandParser {

    fun parseChain(input: String): List<ParsedCommand> {
        val text = input.trim()
        if (text.isEmpty()) return emptyList()

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

        // 1. Dark Mode / Switch theme
        if (lower.contains("dark mode") || lower.contains("dark theme") || lower.contains("switch to dark") || lower.contains("enable dark")) {
            val isOff = lower.contains("off") || lower.contains("light") || lower.contains("disable")
            return ParsedCommand(
                actionType = CommandAction.SWITCH_DARK_MODE,
                primaryTarget = if (isOff) "OFF" else "ON",
                rawCommand = text
            )
        }

        // 2. Open app pattern
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

        // 3. Search pattern
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

        // 4. Click / Tap text
        if (lower.startsWith("click ") || lower.startsWith("tap ") || lower.startsWith("press ")) {
            val target = text.substringAfter(" ").trim()
            return ParsedCommand(
                actionType = CommandAction.CLICK_TEXT,
                primaryTarget = target,
                rawCommand = text
            )
        }

        // 5. Type text
        if (lower.startsWith("type ") || lower.startsWith("enter ") || lower.startsWith("input ")) {
            val value = text.substringAfter(" ").trim()
            return ParsedCommand(
                actionType = CommandAction.TYPE_TEXT,
                primaryTarget = value,
                rawCommand = text
            )
        }

        // 6. Gaming mode pattern
        if (lower.contains("gaming mode")) {
            return ParsedCommand(
                actionType = CommandAction.GAMING_MODE,
                primaryTarget = "Call of Duty Mobile",
                rawCommand = text
            )
        }

        // 7. Volume pattern
        if (lower.contains("volume")) {
            val digits = lower.filter { it.isDigit() }
            val vol = digits.toIntOrNull() ?: 50
            return ParsedCommand(
                actionType = CommandAction.SET_VOLUME,
                numericValue = vol,
                rawCommand = text
            )
        }

        // 8. Do Not Disturb pattern
        if (lower.contains("do not disturb") || lower.contains("dnd")) {
            return ParsedCommand(
                actionType = CommandAction.TOGGLE_DND,
                primaryTarget = if (lower.contains("off") || lower.contains("disable")) "OFF" else "ON",
                rawCommand = text
            )
        }

        // 9. Open website pattern
        if (lower.startsWith("open website") || lower.contains("http://") || lower.contains("https://")) {
            val url = text.substringAfter("website").trim()
            return ParsedCommand(
                actionType = CommandAction.OPEN_WEBSITE,
                primaryTarget = url,
                rawCommand = text
            )
        }

        // 10. Navigation
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


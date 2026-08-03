package com.example.backdoor.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский")
}

enum class StringKey {
    // Terminal Errors
    CMD_NOT_FOUND,
    PERM_DENIED,
    FILE_NOT_FOUND,
    DIR_NOT_FOUND,
    ACCESS_DENIED,
    INVALID_ARG,
    CMD_EXEC_ERROR,
    PATH_ALREADY_EXISTS,
    NOT_A_DIRECTORY,
    NOT_A_FILE,
    UNKNOWN_ERROR,

    // Command Descriptions
    HELP_DESC,
    MAN_DESC,
    CLEAR_DESC,
    ECHO_DESC,
    PWD_DESC,
    CD_DESC,
    LS_DESC,
    TREE_DESC,
    CAT_DESC,
    TOUCH_DESC,
    MKDIR_DESC,
    RM_DESC,
    MV_DESC,
    CP_DESC,
    FIND_DESC,
    HISTORY_DESC,
    WHOAMI_DESC,
    HOSTNAME_DESC,
    DATE_DESC,
    TIME_DESC,
    VERSION_DESC,
    EXIT_DESC,
    OPEN_DESC,
    RENAME_DESC,
    CHMOD_DESC,
    STAT_DESC,
    PING_DESC,
    TRACEROUTE_DESC,
    NETSTAT_DESC,
    IPCONFIG_DESC,
    IFCONFIG_DESC,
    ARP_DESC,
    NSLOOKUP_DESC,
    WHOIS_DESC,
    ROUTE_DESC,

    // UI Labels
    TERMINAL_TITLE,
    SETTINGS_TITLE,
    SYSTEM_MONITOR_TITLE,
    FILES_TITLE,
    LOGS_TITLE,
    NETWORK_TITLE,
    BROWSER_TITLE,
    DESKTOP_TITLE,
    ENTER_COMMAND_PLACEHOLDER,
    QUICK_ACTIONS,
    CLEAR_SCREEN,
    AUTOCMP_SUGGESTIONS,
    
    // System Messages
    BOOT_SUCCESS,
    KERNEL_INITIALIZED
}

object StringManager {
    private val _languageState = MutableStateFlow(Language.ENGLISH)
    val languageState: StateFlow<Language> = _languageState.asStateFlow()

    var currentLanguage: Language
        get() = _languageState.value
        set(value) {
            _languageState.value = value
        }

    fun setLanguage(lang: Language) {
        _languageState.value = lang
    }

    private val enStrings = mapOf(
        StringKey.CMD_NOT_FOUND to "Command '%s' not found. Type 'help' for available commands.",
        StringKey.PERM_DENIED to "Permission denied: %s",
        StringKey.FILE_NOT_FOUND to "File not found: %s",
        StringKey.DIR_NOT_FOUND to "No such directory: %s",
        StringKey.ACCESS_DENIED to "Access denied for resource: %s",
        StringKey.INVALID_ARG to "Invalid argument: %s. %s",
        StringKey.CMD_EXEC_ERROR to "Execution error in '%s': %s",
        StringKey.PATH_ALREADY_EXISTS to "Path already exists: %s",
        StringKey.NOT_A_DIRECTORY to "Not a directory: %s",
        StringKey.NOT_A_FILE to "Not a file: %s",
        StringKey.UNKNOWN_ERROR to "Unknown system error occurred.",

        StringKey.HELP_DESC to "Displays list of available commands or information about a specific command",
        StringKey.MAN_DESC to "Displays manual page for specified command",
        StringKey.CLEAR_DESC to "Clears the terminal screen buffer",
        StringKey.ECHO_DESC to "Prints arguments to standard output with variable expansion",
        StringKey.PWD_DESC to "Prints current working directory path",
        StringKey.CD_DESC to "Changes current working directory",
        StringKey.LS_DESC to "Lists files and directories in specified path",
        StringKey.TREE_DESC to "Displays recursive graphical tree of directory structure",
        StringKey.CAT_DESC to "Displays contents of target file(s)",
        StringKey.TOUCH_DESC to "Creates a new empty file or updates timestamp",
        StringKey.MKDIR_DESC to "Creates a new directory",
        StringKey.RM_DESC to "Removes specified file or directory",
        StringKey.MV_DESC to "Moves or renames file/directory",
        StringKey.CP_DESC to "Copies file or directory to target destination",
        StringKey.FIND_DESC to "Searches for files and directories matching query pattern",
        StringKey.HISTORY_DESC to "Displays or clears command execution history",
        StringKey.WHOAMI_DESC to "Displays current authenticated user handle",
        StringKey.HOSTNAME_DESC to "Displays system node hostname",
        StringKey.DATE_DESC to "Displays current system date",
        StringKey.TIME_DESC to "Displays current system time and uptime",
        StringKey.VERSION_DESC to "Displays AbyssOS kernel version and build architecture",
        StringKey.EXIT_DESC to "Closes terminal application window",
        StringKey.OPEN_DESC to "Launches specified graphical system application",
        StringKey.RENAME_DESC to "Renames a file or directory in Virtual File System",
        StringKey.CHMOD_DESC to "Modifies node permission modes (e.g. 755 or +x)",
        StringKey.STAT_DESC to "Displays detailed file metadata status",
        StringKey.PING_DESC to "Sends ICMP ECHO_REQUEST to virtual network hosts",
        StringKey.TRACEROUTE_DESC to "Traces route packet path to network node",
        StringKey.NETSTAT_DESC to "Prints active network socket connections",
        StringKey.IPCONFIG_DESC to "Displays network interface configuration",
        StringKey.IFCONFIG_DESC to "Configures or displays network interfaces",
        StringKey.ARP_DESC to "Displays ARP address resolution cache table",
        StringKey.NSLOOKUP_DESC to "Queries domain name servers for host IP",
        StringKey.WHOIS_DESC to "Retrieves domain ownership and registration info",
        StringKey.ROUTE_DESC to "Displays or modifies IP routing table",

        StringKey.TERMINAL_TITLE to "Terminal Core 0.7.0",
        StringKey.SETTINGS_TITLE to "System Settings",
        StringKey.SYSTEM_MONITOR_TITLE to "System Monitor",
        StringKey.FILES_TITLE to "File Manager",
        StringKey.LOGS_TITLE to "System Logs",
        StringKey.NETWORK_TITLE to "Network Manager",
        StringKey.BROWSER_TITLE to "Abyss Browser",
        StringKey.DESKTOP_TITLE to "AbyssOS Desktop",
        StringKey.ENTER_COMMAND_PLACEHOLDER to "enter command...",
        StringKey.QUICK_ACTIONS to "Quick Actions",
        StringKey.CLEAR_SCREEN to "Clear",
        StringKey.AUTOCMP_SUGGESTIONS to "Suggestions",

        StringKey.BOOT_SUCCESS to "AbyssOS Offensive Security Framework loaded.",
        StringKey.KERNEL_INITIALIZED to "[KERNEL] AbyssOS v0.7.0 OFFENSIVE SECURITY FRAMEWORK Active."
    )

    private val ruStrings = mapOf(
        StringKey.CMD_NOT_FOUND to "Команда '%s' не найдена. Введите 'help' для списка команд.",
        StringKey.PERM_DENIED to "Отказано в доступе: %s",
        StringKey.FILE_NOT_FOUND to "Файл не найден: %s",
        StringKey.DIR_NOT_FOUND to "Каталог не найден: %s",
        StringKey.ACCESS_DENIED to "Доступ к ресурсу запрещен: %s",
        StringKey.INVALID_ARG to "Неверный аргумент: %s. %s",
        StringKey.CMD_EXEC_ERROR to "Ошибка выполнения '%s': %s",
        StringKey.PATH_ALREADY_EXISTS to "Путь уже существует: %s",
        StringKey.NOT_A_DIRECTORY to "Не является каталогом: %s",
        StringKey.NOT_A_FILE to "Не является файлом: %s",
        StringKey.UNKNOWN_ERROR to "Произошла неизвестная системная ошибка.",

        StringKey.HELP_DESC to "Выводит список доступных команд или справку по конкретной команде",
        StringKey.MAN_DESC to "Отображает руководство пользователю по команде",
        StringKey.CLEAR_DESC to "Очищает экран терминала",
        StringKey.ECHO_DESC to "Выводит текст в консоль с раскрытием переменных",
        StringKey.PWD_DESC to "Выводит текущую рабочую директорию",
        StringKey.CD_DESC to "Переходит в указанную директорию",
        StringKey.LS_DESC to "Выводит список файлов и папок",
        StringKey.TREE_DESC to "Отображает дерево файлов и каталогов",
        StringKey.CAT_DESC to "Выводит содержимое файла",
        StringKey.TOUCH_DESC to "Создает пустой файл",
        StringKey.MKDIR_DESC to "Создает новую директорию",
        StringKey.RM_DESC to "Удаляет файл или директорию",
        StringKey.MV_DESC to "Перемещает или переименовывает объект",
        StringKey.CP_DESC to "Копирует файл или директорию",
        StringKey.FIND_DESC to "Ищет файлы и директории по имени",
        StringKey.HISTORY_DESC to "Выводит историю выполненных команд",
        StringKey.WHOAMI_DESC to "Отображает имя текущего пользователя",
        StringKey.HOSTNAME_DESC to "Отображает имя хоста системы",
        StringKey.DATE_DESC to "Отображает системную дату",
        StringKey.TIME_DESC to "Отображает системное время и время работы",
        StringKey.VERSION_DESC to "Отображает версию ядра AbyssOS",
        StringKey.EXIT_DESC to "Закрывает окно терминала",
        StringKey.OPEN_DESC to "Запускает графическое приложение",
        StringKey.RENAME_DESC to "Переименовывает файл или каталог",
        StringKey.CHMOD_DESC to "Изменяет права доступа",
        StringKey.STAT_DESC to "Отображает метаданные файла",
        StringKey.PING_DESC to "Отправляет ICMP ECHO-запросы узлам виртуальной сети",
        StringKey.TRACEROUTE_DESC to "Трассирует маршрут пакетов к узлу сети",
        StringKey.NETSTAT_DESC to "Выводит активные сетевые подключения",
        StringKey.IPCONFIG_DESC to "Отображает конфигурацию сетевых интерфейсов",
        StringKey.IFCONFIG_DESC to "Настраивает или отображает сетевые интерфейсы",
        StringKey.ARP_DESC to "Выводит таблицу ARP-кэша адресов",
        StringKey.NSLOOKUP_DESC to "Запрашивает DNS-серверы для поиска IP",
        StringKey.WHOIS_DESC to "Получает информацию о владельце домена",
        StringKey.ROUTE_DESC to "Отображает таблицу маршрутизации IP",

        StringKey.TERMINAL_TITLE to "Терминал 0.7.0",
        StringKey.SETTINGS_TITLE to "Настройки системы",
        StringKey.SYSTEM_MONITOR_TITLE to "Монитор ресурсов",
        StringKey.FILES_TITLE to "Файловый менеджер",
        StringKey.LOGS_TITLE to "Системные логи",
        StringKey.NETWORK_TITLE to "Сетевой менеджер",
        StringKey.BROWSER_TITLE to "Браузер Abyss",
        StringKey.DESKTOP_TITLE to "Рабочий стол AbyssOS",
        StringKey.ENTER_COMMAND_PLACEHOLDER to "введите команду...",
        StringKey.QUICK_ACTIONS to "Быстрые действия",
        StringKey.CLEAR_SCREEN to "Очистить",
        StringKey.AUTOCMP_SUGGESTIONS to "Подсказки",

        StringKey.BOOT_SUCCESS to "Загружена система безопасности AbyssNet Offensive Security Framework.",
        StringKey.KERNEL_INITIALIZED to "[ЯДРО] AbyssOS v0.7.0 OFFENSIVE SECURITY FRAMEWORK Активно."
    )

    fun get(key: StringKey, vararg args: Any): String {
        val map = when (currentLanguage) {
            Language.RUSSIAN -> ruStrings
            else -> enStrings
        }
        val template = map[key] ?: enStrings[key] ?: key.name
        return if (args.isNotEmpty()) {
            try {
                String.format(template, *args)
            } catch (e: Exception) {
                template
            }
        } else {
            template
        }
    }
}

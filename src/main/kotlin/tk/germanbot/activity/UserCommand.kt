package tk.germanbot.activity

import java.util.*

enum class UserCommand(val textCommand: String) {
    END("#end"),
    HINT("#h"),
    CANCEL("#cancel");

    fun eq(str: String): Boolean =
            UserCommand.parse(str)
                    .map { cmd -> cmd == this }
                    .orElse(false)

    companion object {

        fun parse(value: String?): Optional<UserCommand> {
            if (value == null)
                return Optional.empty()
            val trimmedValue = value.trim()
            return Arrays.stream(values())
                    .filter { cmd ->
                        cmd.textCommand.equals(trimmedValue, true)
                                || cmd.name.equals(trimmedValue, true)
                    }
                    .findAny()
        }
    }
}
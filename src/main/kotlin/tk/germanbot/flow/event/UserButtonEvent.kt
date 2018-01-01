package tk.germanbot.flow.event

import tk.germanbot.fsm.Event
import java.util.*

enum class UserCommand(val textCommand: String) {
    CANCEL("#cancel");

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

class UserButtonEvent(
        val userId: String,
        val button: UserCommand) : Event()
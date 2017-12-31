package tk.germanbot.flow.event

import tk.germanbot.fsm.Event
import java.util.*

enum class UserButton {
    CANCEL;

    companion object {

        fun parse(value: String?): Optional<UserButton> =
                try {
                    if (value != null) Optional.of(UserButton.valueOf(value)) else Optional.empty()
                } catch (e: Exception) {
                    Optional.empty()
                }
    }
}

class UserButtonEvent(
        val userId: String,
        val button: UserButton) : Event()
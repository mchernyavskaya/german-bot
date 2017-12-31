package tk.germanbot.flow

import tk.germanbot.fsm.State
import java.util.*

interface StateService {
    fun getState(userId: String): Optional<State>
    fun saveState(state: State)
}
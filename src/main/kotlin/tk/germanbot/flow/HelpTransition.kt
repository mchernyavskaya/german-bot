package tk.germanbot.flow

import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition
import tk.germanbot.fsm.isTextMessage
import java.util.stream.Collectors

class HelpTransition(
        private val fsnFactory: FsmFactory,
        private val state: State,
        override val helpText: String = "") : Transition() {

    override fun accept(event: Event) = isTextMessage(event, "?")

    override fun follow(event: Event): State {
        val joinedHelptext = state.transitions.reversed().stream()
                .filter { tr -> tr.helpText != "" }
                .map { tr -> tr.helpText }
                .collect(Collectors.joining("\n"))
        fsnFactory.msgGateway.textMessage(state.stateData.userId, joinedHelptext)
        return state
    }

}
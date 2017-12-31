package tk.germanbot.flow

import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition
import tk.germanbot.fsm.isTextMessage

class HelpTransition(val fsnFactory: FsmFactory, val state: State, val helpText: String) : Transition() {

    override fun accept(event: Event) = isTextMessage(event, "?")

    override fun follow(event: Event): State {
        fsnFactory.msgGateway.textMessage(state.stateData.userId, helpText)
        return state
    }

}
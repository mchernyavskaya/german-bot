package tk.germanbot.flow

import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition

class WtfTransition(val msgGateway: MessageGateway, val state: State, val message: String) : Transition() {

    override fun accept(event: Event) = true

    override fun follow(event: Event): State {
        msgGateway.textMessage(state.stateData.userId, message);
        return state
    }

}
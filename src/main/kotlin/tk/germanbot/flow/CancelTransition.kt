package tk.germanbot.flow

import tk.germanbot.flow.event.UserButton
import tk.germanbot.flow.event.UserButtonEvent
import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition

class CancelTransition(val fsmFactory: FsmFactory, val state: State, val message: String) : Transition() {

    override fun accept(event: Event) = event is UserButtonEvent && event.button == UserButton.CANCEL

    override fun follow(event: Event): State {
        fsmFactory.msgGateway.textMessage(state.stateData.userId, message)
        return fsmFactory.createReadyState(state.stateData.userId)
    }

}
package tk.germanbot.flow.populate

import tk.germanbot.flow.FsmFactory
import tk.germanbot.flow.event.UserTextMessageEvent
import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition

class AddQuizTransition(val fsnFactory: FsmFactory, val state: State) : Transition() {

    override fun accept(event: Event) = event is UserTextMessageEvent

    override fun follow(event: Event): State {
        val evt = event as UserTextMessageEvent
        fsnFactory.msgGateway.messageWithCancelButton(state.stateData.userId, "Add quiz?")
        return fsnFactory.createAddQuizState(state.stateData.userId, evt.message)
    }

}
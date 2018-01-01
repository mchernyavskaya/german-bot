package tk.germanbot.flow

import tk.germanbot.flow.event.UserButtonEvent
import tk.germanbot.flow.event.UserCommand
import tk.germanbot.flow.event.UserTextMessageEvent
import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.Transition

class CancelTransition(val fsmFactory: FsmFactory, val state: State, val message: String) : Transition() {

    override val helpText = "or type #cancel"

    override fun accept(event: Event) =
            (event is UserButtonEvent && event.button == UserCommand.CANCEL) ||
                    (event is UserTextMessageEvent &&
                            UserCommand.parse(event.message)
                                    .filter { cmd -> cmd == UserCommand.CANCEL }
                                    .isPresent)

    override fun follow(event: Event): State {
        fsmFactory.msgGateway.textMessage(state.stateData.userId, message)
        return fsmFactory.createReadyState(state.stateData.userId)
    }

}
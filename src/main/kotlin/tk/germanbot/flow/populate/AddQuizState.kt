package tk.germanbot.flow.populate

import tk.germanbot.flow.FsmFactory
import tk.germanbot.flow.HelpTransition
import tk.germanbot.flow.event.UserTextMessageEvent
import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.StateData
import tk.germanbot.fsm.Transition

data class AddQuizStateData(
        override val userId: String,
        val userText: String
                            ) : StateData
open class AddQuizState(
        private val fsnFactory : FsmFactory,
        override val stateData: AddQuizStateData) : State(stateData) {

    override val transitions: List<Transition>
        get() = listOf(
                HelpTransition(fsnFactory, this, ""),
                fsnFactory.createCancelTransition(this),
                AddAnswerTransition(fsnFactory, this)
                )
    override val unknownEventTransition: Transition
        get() = fsnFactory.createWtfTransition(this, "What? I expected some text")

}

class AddAnswerTransition(private val fsnFactory: FsmFactory, private val state: AddQuizState) : Transition() {

    override val helpText = "Send me an answer to that quiz and I'll store it"

    override fun accept(event: Event) = event is UserTextMessageEvent

    override fun follow(event: Event): State {
        val evt = event as UserTextMessageEvent
        fsnFactory.quizService.saveQuiz(state.stateData.userId, state.stateData.userText, evt.message);
        fsnFactory.msgGateway.textMessage(state.stateData.userId, "Stored ;-)");
        return fsnFactory.createReadyState(state.stateData.userId)
    }

}
package tk.germanbot.flow

import tk.germanbot.flow.populate.AddQuizTransition
import tk.germanbot.flow.quiz.StartQuizTransition
import tk.germanbot.fsm.State;
import tk.germanbot.fsm.StateData
import tk.germanbot.fsm.Transition


data class ReadyStateData(override val userId: String) : StateData

class ReadyState(
        private val fsnFactory :FsmFactory,
        override val stateData: ReadyStateData) : State(stateData) {

    override val transitions: List<Transition>
        get() = listOf(
                HelpTransition(fsnFactory, this, ""),
                StartQuizTransition(fsnFactory, this),
                AddQuizTransition(fsnFactory, this))
    override val unknownEventTransition: Transition
        get() = fsnFactory.createWtfTransition(this, "What?")

}


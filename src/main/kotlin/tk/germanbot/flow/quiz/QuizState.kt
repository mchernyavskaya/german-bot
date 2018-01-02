package tk.germanbot.flow.quiz

import tk.germanbot.flow.Correctness
import tk.germanbot.flow.FsmFactory
import tk.germanbot.flow.HelpTransition
import tk.germanbot.flow.event.UserTextMessageEvent
import tk.germanbot.fsm.Event
import tk.germanbot.fsm.State
import tk.germanbot.fsm.StateData
import tk.germanbot.fsm.Transition

data class QuizStateData(
        override val userId: String,
        val quizId: String) : StateData

open class QuizState(
        private val fsnFactory: FsmFactory,
        override val stateData: QuizStateData) : State(stateData) {

    override val transitions: List<Transition>
        get() = listOf(
                HelpTransition(fsnFactory, this),
                fsnFactory.createCancelTransition(this),
                GiveUpTransition(fsnFactory, this),
                AnswerGivenTransition(fsnFactory, this)
        )
    override val unknownEventTransition: Transition
        get() = fsnFactory.createWtfTransition(this, "What? I expected some answer")

}

class StartQuizTransition(
        private val fsnFactory: FsmFactory,
        private val state: State) : Transition() {

    override val helpText = "#q - start quiz"

    override fun accept(event: Event) =
            event is UserTextMessageEvent && event.message.trim() == "#q"

    override fun follow(event: Event): State {
        val quiz = fsnFactory.quizService.getQuestion(state.stateData.userId)
        fsnFactory.msgGateway.textMessage(state.stateData.userId, quiz.question)
        return QuizState(fsnFactory, QuizStateData(state.stateData.userId, quiz.id))
    }

}

class AnswerGivenTransition(
        private val fsnFactory: FsmFactory,
        private val state: QuizState) : Transition() {

    override val helpText = "Send me the answer"

    override fun accept(event: Event) = event is UserTextMessageEvent

    override fun follow(event: Event): State {
        val evt = event as UserTextMessageEvent

        val valuation = fsnFactory.quizService.checkAnswer(state.stateData.userId, state.stateData.quizId, evt.message)
        return when (valuation.result) {
            Correctness.CORRECT -> {
                fsnFactory.msgGateway.textMessage(state.stateData.userId, "Correct! (y)")
                fsnFactory.createReadyState(state.stateData.userId)
            }
            Correctness.PARTIALLY_CORRECT -> {
                fsnFactory.msgGateway.textMessage(state.stateData.userId, "Almost! Answer:\n" + valuation.correctAnswer)
                fsnFactory.createReadyState(state.stateData.userId)
            }
            else -> {
                fsnFactory.msgGateway.textMessage(state.stateData.userId, "Erm, not quite. Answer:\n" + valuation.correctAnswer)
                fsnFactory.createReadyState(state.stateData.userId)
            }
        }
    }

}

class GiveUpTransition(
        private val fsnFactory: FsmFactory,
        private val state: QuizState) : Transition() {

    override val helpText = "#g - give up, show the answer"

    override fun accept(event: Event) =
            event is UserTextMessageEvent && event.message.trim() == "#g"

    override fun follow(event: Event): State {
        val answer = fsnFactory.quizService.getAnswer(state.stateData.userId, state.stateData.quizId)
        fsnFactory.msgGateway.textMessage(state.stateData.userId, "Answer:\n" + answer)
        return fsnFactory.createReadyState(state.stateData.userId)
    }

}


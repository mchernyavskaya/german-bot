package tk.germanbot.flow

import tk.germanbot.flow.populate.AddQuizState
import tk.germanbot.flow.populate.AddQuizStateData
import tk.germanbot.fsm.State

open class FsmFactory(
        open val msgGateway: MessageGateway,
        val quizService: QuizService) {

    fun createAddQuizState(userId: String, userText: String) = AddQuizState(this, AddQuizStateData(userId, userText))

    fun createReadyState(userId: String) = ReadyState(this, ReadyStateData(userId))

    fun createWtfTransition(state: State, message: String) = WtfTransition(msgGateway, state, message)

    fun createCancelTransition(state: State) = CancelTransition(this, state, "Ok, forget.")

}
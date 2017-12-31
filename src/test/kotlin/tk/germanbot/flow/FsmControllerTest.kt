package tk.germanbot.flow

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import tk.germanbot.flow.event.UserButton
import tk.germanbot.flow.event.UserButtonEvent
import tk.germanbot.flow.event.UserTextMessageEvent
import tk.germanbot.flow.populate.AddQuizState
import tk.germanbot.fsm.State
import java.util.*

@RunWith(MockitoJUnitRunner::class)
internal class FsmControllerTest{

    private val msgGateway: MessageGateway = mock()
    private val quizService: QuizService = mock()
    private val stateService: StateService = mock()

    private val fsmController: FsmController

    init {
        val fsmFactory = FsmFactory(msgGateway, quizService)
        fsmController = FsmController(fsmFactory, stateService)
    }

    @Test
    fun acceptEventCanHandleTextWhenNoPrevState() {
        whenever(stateService.getState(any())).thenReturn(Optional.empty())

        val savedState = userSendsQuiz()

        reset(msgGateway)
        reset(quizService)
        reset(stateService)

        whenever(stateService.getState(any())).thenReturn(Optional.of(savedState))

        // User sends an answer
        fsmController.acceptEvent("user1", UserTextMessageEvent("user1", "some answer"))
        verify(msgGateway).textMessage("user1", "Stored ;-)")
        verify(quizService).saveQuiz("user1", "some quiz", "some answer")
        argumentCaptor<State>().apply {
            verify(stateService).saveState(capture())
            assertThat(firstValue is ReadyState).isTrue()
        }
    }

    @Test
    fun acceptEventCanHandleCancel() {
        whenever(stateService.getState(any())).thenReturn(Optional.empty())

        val savedState = userSendsQuiz()

        reset(msgGateway)
        reset(quizService)
        reset(stateService)

        whenever(stateService.getState(any())).thenReturn(Optional.of(savedState))

        // User sends Cancel
        fsmController.acceptEvent("user1", UserButtonEvent("user1", UserButton.CANCEL))
        verify(msgGateway).textMessage("user1", "Ok, forget.")
        argumentCaptor<State>().apply {
            verify(stateService).saveState(capture())
            assertThat(firstValue is ReadyState).isTrue()
        }
    }

    private fun userSendsQuiz(): State {
        // User sends a quiz
        fsmController.acceptEvent("user1", UserTextMessageEvent("user1", "some quiz"))

        verify(msgGateway).messageWithCancelButton("user1", "Add quiz?")
        val stateCaptor = argumentCaptor<State>()
        verify(stateService).saveState(stateCaptor.capture())

        val savedState = stateCaptor.firstValue
        assertThat(savedState is AddQuizState).isTrue()
        assertThat(savedState.stateData.userId).isEqualTo("user1")
        assertThat((savedState as AddQuizState).stateData.userText).isEqualTo("some quiz")
        return savedState
    }
}
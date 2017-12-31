package tk.germanbot.flow

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import tk.germanbot.flow.event.UserTextMessageEvent

@RunWith(MockitoJUnitRunner::class)
class ReadyStateTest {

    private val msgGateway: MessageGateway = mock()
    private val fsmFactory: FsmFactory = mock()
    private val state: ReadyState

    init{
        state = ReadyState(fsmFactory, ReadyStateData("user1"))
    }

    @Test
    fun nextStateShouldReturnOneState() {
        whenever(fsmFactory.msgGateway).thenReturn(msgGateway)

        state.nextState(UserTextMessageEvent("userId", "?"))

        verify(msgGateway).textMessage(eq("user1"), any())
    }

}
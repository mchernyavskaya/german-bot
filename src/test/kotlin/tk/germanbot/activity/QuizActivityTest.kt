package tk.germanbot.activity

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import tk.germanbot.activity.lesson.QuizActivity
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService


@RunWith(MockitoJUnitRunner::class)
class QuizActivityTest {
    @Mock
    var activityManager: ActivityManager? = null
    @Mock
    var messageGateway: MessageGateway? = null
    @Mock
    val quizService: QuizService? = null

    @InjectMocks
    var activity: QuizActivity? = null


    @Test
    fun onCorrectEvent() {
        // TODO
    }

}
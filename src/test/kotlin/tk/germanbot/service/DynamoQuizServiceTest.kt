package tk.germanbot.service

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import tk.germanbot.data.Quiz
import tk.germanbot.data.QuizRepository

@RunWith(MockitoJUnitRunner::class)
class DynamoQuizServiceTest {

    @Mock
    private var quizRepo: QuizRepository? = null

    @Mock
    private var quizValidator: QuizValidator? = null

    @Mock
    private val statService: UserStatService? = null

    @InjectMocks
    private var service: DynamoQuizService? = null

    @Test
    fun saveQuizCanParseTopicsAndAnswers() {
        service!!.saveQuiz("userId", "Question with topics  #A1 #some_topic", "  answer1 + this is answer2  ")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("userId")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with topics")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly("A1", "some_topic")
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1", "this is answer2")
    }

    @Test
    fun saveQuizCanParseTopicsAndAnswersSimpleCase() {
        service!!.saveQuiz("userId", "Question with no topics ", "  answer1")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("userId")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with no topics")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly("undefined")
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1")
    }

    @Test
    fun saveQuizCanParseTopicsAndAnswersEmptyCase() {
        service!!.saveQuiz("userId", "Question with empty topics # ## ## ", "  answer1+")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("userId")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with empty topics # ## ##")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly("undefined")
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1")
    }

}
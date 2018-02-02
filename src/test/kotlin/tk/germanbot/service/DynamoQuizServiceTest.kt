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
import tk.germanbot.data.QuizTopic
import tk.germanbot.data.QuizTopicRepository

@RunWith(MockitoJUnitRunner::class)
class DynamoQuizServiceTest {

    @Mock
    private var quizRepo: QuizRepository? = null

    @Mock
    private var quizTopicRepo: QuizTopicRepository? = null

    @Mock
    private var quizValidator: QuizValidator? = null

    @Mock
    private val statService: UserStatService? = null

    @InjectMocks
    private var service: DynamoQuizService? = null

    @Test
    fun saveQuizCanParseTopicsAndAnswers() {
        service!!.saveQuiz("id", "Question with topics  #A1 #some_topic", "  answer1 + this is answer2  ")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("id")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with topics")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly("A1", "some_topic")
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1", "this is answer2")
    }

    @Test
    fun saveQuizCanParseTopicsAndAnswersSimpleCase() {
        service!!.saveQuiz("id", "Question with no topics ", "  answer1")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("id")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with no topics")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly(QuizTopic.UNDEFINED)
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1")
    }

    @Test
    fun saveQuizCanParseTopicsAndAnswersEmptyCase() {
        service!!.saveQuiz("id", "Question with empty topics # ## ## ", "  answer1+")

        val quizCaptor = argumentCaptor<Quiz>()
        verify(quizRepo!!).save(quizCaptor.capture())

        Assertions.assertThat(quizCaptor.firstValue.createdBy).isEqualTo("id")
        Assertions.assertThat(quizCaptor.firstValue.question).isEqualTo("Question with empty topics # ## ##")
        Assertions.assertThat(quizCaptor.firstValue.topics).containsExactly(QuizTopic.UNDEFINED)
        Assertions.assertThat(quizCaptor.firstValue.answers).containsExactly("answer1")
    }

    @Test
    fun randomSelectCanSelectExactCount() {
        val list = listOf("A", "B", "C")
        val select1 = service!!.randomSelect(list, 1)
        Assertions.assertThat(select1).hasSize(1)

        val select2 = service!!.randomSelect(list, 2)
        Assertions.assertThat(select2).hasSize(2)

        val select3 = service!!.randomSelect(list, 3)
        Assertions.assertThat(select3).hasSize(3)

        val select4 = service!!.randomSelect(list, 4)
        Assertions.assertThat(select4).hasSize(3)

        val select5 = service!!.randomSelect(list, 5)
        Assertions.assertThat(select5).hasSize(3)
    }

    @Test
    fun randomSelect10from100() {
        val ints = (0..100).toList()
        val select1 = service!!.randomSelect(ints, 10)
        print(select1)
        Assertions.assertThat(select1).hasSize(10)
    }

}
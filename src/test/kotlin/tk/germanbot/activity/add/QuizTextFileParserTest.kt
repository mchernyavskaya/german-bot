package tk.germanbot.activity.add

import org.assertj.core.api.Assertions
import org.junit.Test
import tk.germanbot.service.QuizTextFileParser

class QuizTextFileParserTest {
    @Test
    fun getQuizzesShouldParseSimpleQuiz() {
        val simple = """
Question
Answer1
Answer2
#topic1 #topic2

"""

        val q = QuizTextFileParser("userId", simple).getQuizzes()

        Assertions.assertThat(q).hasSize(1)
        Assertions.assertThat(q.first().question).isEqualTo("Question")
        Assertions.assertThat(q.first().answers).containsExactly("Answer1", "Answer2")
        Assertions.assertThat(q.first().topics).containsExactly("topic1", "topic2")

    }

    @Test
    fun getQuizzesShouldParseTwo() {
        val simple = """

Question1
Answer11
Answer12
#topic11 #topic12


Question2
Answer21
Answer22
#topic21 #topic22


"""

        val q = QuizTextFileParser("userId", simple).getQuizzes()

        Assertions.assertThat(q).hasSize(2)
        Assertions.assertThat(q[0].question).isEqualTo("Question1")
        Assertions.assertThat(q[0].answers).containsExactly("Answer11", "Answer12")
        Assertions.assertThat(q[0].topics).containsExactly("topic11", "topic12")
        Assertions.assertThat(q[1].question).isEqualTo("Question2")
        Assertions.assertThat(q[1].answers).containsExactly("Answer21", "Answer22")
        Assertions.assertThat(q[1].topics).containsExactly("topic21", "topic22")

    }

    @Test
    fun getQuizzesShouldParseTwoDiff() {
        val simple = """

Question1
Answer11


Question2
#topic21
Answer21
Answer22
#topic22


"""

        val q = QuizTextFileParser("userId", simple).getQuizzes()

        Assertions.assertThat(q).hasSize(2)
        Assertions.assertThat(q[0].question).isEqualTo("Question1")
        Assertions.assertThat(q[0].answers).containsExactly("Answer11")
        Assertions.assertThat(q[0].topics).isEmpty()
        Assertions.assertThat(q[1].question).isEqualTo("Question2")
        Assertions.assertThat(q[1].answers).containsExactly("Answer21", "Answer22")
        Assertions.assertThat(q[1].topics).containsExactly("topic21", "topic22")

    }

    @Test
    fun getQuizzesShouldParseGlobalTopics() {
        val simple = """

#A1
Question1
Answer11
Answer12
#topic11 #topic12


Question2
Answer21
Answer22
#topic21 #topic22


"""

        val q = QuizTextFileParser("userId", simple).getQuizzes()

        Assertions.assertThat(q).hasSize(2)
        Assertions.assertThat(q[0].question).isEqualTo("Question1")
        Assertions.assertThat(q[0].answers).containsExactly("Answer11", "Answer12")
        Assertions.assertThat(q[0].topics).containsExactly("topic11", "topic12", "A1")
        Assertions.assertThat(q[1].question).isEqualTo("Question2")
        Assertions.assertThat(q[1].answers).containsExactly("Answer21", "Answer22")
        Assertions.assertThat(q[1].topics).containsExactly("topic21", "topic22", "A1")

    }

}
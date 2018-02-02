package tk.germanbot.data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.WelcomeActivityData
import tk.germanbot.activity.lesson.LessonActivityData
import tk.germanbot.activity.lesson.QuizActivityData
import tk.germanbot.service.Correctness

class PolymorphDeserializationTest {

    @Test
    fun canDeserializeList() {
        val d1 = WelcomeActivityData("userId1")
        d1.isHelloSaid = true
        val d2 = LessonActivityData("userId2")
        d2.answeredQuestions = 2
        d2.desiredQuestions = 10
        d2.totalQuestions = 5
        val d3 = QuizActivityData("id", "quizId")
        d3.result = Correctness.CORRECT

        val list: List<ActivityData> = object : ArrayList<ActivityData>(listOf(d1, d2, d3)) {}

        val mapper = ObjectMapper()
        val json: String = mapper.writeValueAsString(list)

        val restored: List<ActivityData> = mapper.readValue(json, object : TypeReference<List<ActivityData>>() {})

        assertThat(restored[0]).hasSameClassAs(d1)
        assertThat(restored[1]).hasSameClassAs(d2)
        assertThat(restored[2]).hasSameClassAs(d3)
        assertThat((restored[2] as QuizActivityData).quizId).isEqualTo("quizId")
    }

}
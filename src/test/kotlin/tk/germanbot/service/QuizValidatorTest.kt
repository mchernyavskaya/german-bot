package tk.germanbot.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuizValidatorTest {
    private val validator: QuizValidator = QuizValidator()

    @Test
    fun validateFullyCorrect() {
        val validate = validator.validate("answer", setOf("answer"))
        assertThat(validate.result).isEqualTo(Correctness.CORRECT)
    }

    @Test
    fun validateNotCorrect() {
        val validate = validator.validate("answer123", setOf("answer"))
        assertThat(validate.result).isEqualTo(Correctness.INCORRECT)
    }

    @Test
    fun validateDifferentCase() {
        val validate = validator.validate("AnsWer", setOf("answer"))
        assertThat(validate.result).isEqualTo(Correctness.CORRECT)
    }

    @Test
    fun validateDifferentPunctuation() {
        val validate = validator.validate("answer!!...", setOf("answer"))
        assertThat(validate.result).isEqualTo(Correctness.CORRECT)
    }

    @Test
    fun validateDifferentSpaces() {
        val validate = validator.validate("answer  answer", setOf("answer answer"))
        assertThat(validate.result).isEqualTo(Correctness.CORRECT)
    }
}
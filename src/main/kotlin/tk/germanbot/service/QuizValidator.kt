package tk.germanbot.service

import org.springframework.stereotype.Component

@Component
class QuizValidator {
    private val punctuation = Regex("\\W+")
    private val spaces = Regex("\\s+")

    fun validate(answerGiven: String, correctAnswers: Set<String>): AnswerValidationResult {
        correctAnswers
                .filter { isCorrect(answerGiven, it) }
                .forEach { return AnswerValidationResult(Correctness.CORRECT) }

        correctAnswers
                .filter { isPartialCorrect(answerGiven, it) }
                .forEach { return AnswerValidationResult(Correctness.PARTIALLY_CORRECT, it) }

        return AnswerValidationResult(Correctness.INCORRECT, correctAnswers.first())
    }

    private fun isCorrect(answerGiven: String, correctAnswer: String): Boolean {
        val answerGivenFixed = answerGiven.replace(punctuation, "").replace(spaces, " ").trim()
        val correctAnswerFixed = correctAnswer.replace(punctuation, "").replace(spaces, " ").trim()
        return answerGivenFixed.equals(correctAnswerFixed, true)
    }

    private fun isPartialCorrect(answerGiven: String, correctAnswer: String): Boolean {
        // TODO: do fuzzy check, e.g. diff in word ending is fine
        return isCorrect(answerGiven, correctAnswer)
    }

}

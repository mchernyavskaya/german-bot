package tk.germanbot.service

import org.springframework.stereotype.Component

@Component
class QuizValidator {

    fun validate(answerGiven: String, correctAnswers: Set<String>): AnswerValidationResult {
        correctAnswers
                .filter { isCorrect(answerGiven, it) }
                .forEach { return AnswerValidationResult(Correctness.CORRECT) }

        correctAnswers
                .filter { isPartialCorrect(answerGiven, it) }
                .forEach { return AnswerValidationResult(Correctness.PARTIALLY_CORRECT, it) }

        return AnswerValidationResult(Correctness.INCORRECT, correctAnswers.first())
    }

    fun isCorrect(answerGiven: String, correctAnswer: String): Boolean {
        return answerGiven.equals(correctAnswer, true)
    }

    fun isPartialCorrect(answerGiven: String, correctAnswer: String): Boolean {
        // todo: do fuzzy check, e.g. diff in word ending is fine
        return answerGiven.equals(correctAnswer, true)
    }

}

package tk.germanbot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.germanbot.data.Quiz
import tk.germanbot.data.QuizRepository
import java.util.Collections
import java.util.UUID

@Service
class DynamoQuizService(
        @Autowired private val quizRepo: QuizRepository,
        @Autowired private val quizValidator: QuizValidator
) : QuizService {

    override fun saveQuiz(userId: String, question: String, answer: String) {
        val (q, topics) = extractTopics(question)

        val answers = answer.split("+")
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()

        val quiz = Quiz(createdBy = userId, question = q, answers = answers, topics = topics)
        quiz.validate()
        quizRepo.save(quiz)
    }

    private data class AnswersTopics(
            val question: String,
            val topics: Set<String>)

    private fun extractTopics(question: String): AnswersTopics {
        val topicRegex = Regex("#(\\w+)")

        val topics = topicRegex.findAll(question)
                .map { it.groupValues[1] }
                .filter(String::isNotBlank)
                .toSet()
                .let { if (it.isEmpty()) setOf("undefined") else it }

        val q = topicRegex.replace(question, "").trim()

        return AnswersTopics(q, topics)
    }

    override fun checkAnswer(quizId: String, answer: String): AnswerValidationResult {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        quiz.validate()
        //todo: save answer statistic
        return quizValidator.validate(answer, quiz.answers!!)
    }

    override fun getAnswer(quizId: String): String {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        quiz.validate()
        return quiz.answers!!.first()
    }

    override fun getQuiz(quizId: String): Quiz {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        quiz.validate()
        return quiz
    }

    override fun getQuestionIds(userId: String, totalQuestions: Int): List<String> {
        //todo: implement question selection strategy
        val randomKey = UUID.randomUUID().toString()
        val qGreater = quizRepo.findTop5ByIdGreaterThan(randomKey)
        val qLess = if (qGreater.size < 5) quizRepo.findTop5ByIdLessThan(randomKey) else qGreater
        val qq = if (qLess.size > qGreater.size) qLess else qGreater
        val mutable = qq.toMutableList()
        Collections.shuffle(mutable)
        return mutable
                .map { q -> q.id!! }
                .toList()
    }

}
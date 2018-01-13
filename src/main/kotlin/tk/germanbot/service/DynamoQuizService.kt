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

    override fun saveQuiz(question: String, answer: String) {
        val quiz = Quiz(question = question, answers = setOf(answer), topics = setOf("undefined"))
        quiz.validate()
        quizRepo.save(quiz)
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
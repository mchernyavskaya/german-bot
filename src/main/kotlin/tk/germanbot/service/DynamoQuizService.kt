package tk.germanbot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.germanbot.data.Quiz
import tk.germanbot.data.QuizRepository
import tk.germanbot.data.QuizTopic
import tk.germanbot.data.QuizTopicRepository
import java.util.Collections
import java.util.Random

@Service
class DynamoQuizService(
        @Autowired private val quizRepo: QuizRepository,
        @Autowired private val quizTopicRepo: QuizTopicRepository,
        @Autowired private val quizValidator: QuizValidator,
        @Autowired private val statService: UserStatService
) : QuizService {

    override fun saveQuiz(userId: String, quiz: Quiz): Quiz {
        quiz.validate()

        quiz.question = if (quiz.question != null) removeFormatting(quiz.question!!) else null
        quiz.answers = quiz.answers?.map(this::removeFormatting)?.toSet()

        val quiz = quizRepo.save(quiz)
        // saving of topics must be after quiz since quizId can be auto-generated
        quizTopicRepo.saveTopics(quiz)
        return quiz
    }

    private fun removeFormatting(question: String): String {
        return question?.replace(Regex("\\*|_|`"), "")
    }

    override fun saveQuiz(userId: String, question: String, answer: String): Quiz {
        val (q, topics) = extractTopics(question)

        val answers = answer.split("+")
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()

        return saveQuiz(userId, Quiz(createdBy = userId, question = q, answers = answers, topics = topics))
    }

    override fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        val validationResult = quizValidator.validate(answer, quiz.answers!!)

        statService.updateQuizStat(userId, quizId, quiz.topics!!, validationResult.result != Correctness.INCORRECT)

        return validationResult
    }

    override fun getAnswer(quizId: String): String {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        return quiz.answers!!.first()
    }

    override fun getQuiz(quizId: String): Quiz {
        val quiz = quizRepo.findOneById(quizId) ?: throw EntityNotFoundException(Quiz::class, quizId)
        return quiz
    }

    override fun selectQuizzesForUser(userId: String, topics: Set<String>, totalQuestions: Int): List<String> {
        val randomQuizSelection = (quizTopicRepo.findNRandomQuizIdsByTopics(topics + QuizTopic.PUBLISHED, totalQuestions * 5)
                + quizTopicRepo.findNRandomQuizIdsByTopics(topics + userId, totalQuestions * 5))
        return randomSelect(randomQuizSelection, totalQuestions)
                .map { qt -> qt.quizId!! }
    }

//    private fun getQuestionIds(userId: String, totalQuestions: Int): List<String> {
//        //todo: implement question selection strategy
//        val randomKey = UUID.randomUUID().toString()
//        val qGreater = quizRepo.findTop50ByIdGreaterThan(randomKey)
//        val qLess = if (qGreater.size < totalQuestions) quizRepo.findTop50ByIdLessThan(randomKey) else qGreater
//        val qq = if (qLess.size > qGreater.size) qLess else qGreater
//
//        return randomSelect(qq.toMutableList(), totalQuestions)
//                .map { q -> q.id!! }
//                .toList()
//    }

    internal fun <T> randomSelect(quizIdsByTopics: List<T>, count: Int): List<T> {
        val selected = mutableListOf<T>()
        val rnd = Random()
        for (i in quizIdsByTopics.indices) {
            val selectProbability = (count.toDouble() - selected.size) / (quizIdsByTopics.size - i)
            if (rnd.nextDouble() <= selectProbability) {
                selected.add(quizIdsByTopics[i])
            }
            if (selected.size == count) {
                break
            }
        }

        Collections.shuffle(selected)
        return selected
    }

    override fun getAll(): List<Quiz> {
        return quizRepo.findAll()
    }

    private data class ParsedTopics(
            val question: String,
            val topics: Set<String>)

    private fun extractTopics(question: String): ParsedTopics {
        val topicRegex = Regex("#(\\w+)")

        val topics = topicRegex.findAll(question)
                .map { it.groupValues[1] }
                .filter(String::isNotBlank)
                .toSet()
                .let { if (it.isEmpty()) setOf(QuizTopic.UNDEFINED) else it }

        val q = topicRegex.replace(question, "").trim()

        return ParsedTopics(q, topics)
    }

}
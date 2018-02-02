package tk.germanbot.service

import org.springframework.stereotype.Service

@Service
class HintService {
    private val PUNCTUATION_RX: Regex = Regex("\\W")
    private val WHITESPACE_RX: Regex = Regex("\\s+")

    /**
     * Create a hint from current value
     * (Opens as many symbols in each word as the second parameter says)
     */
    fun hint(answer: String, open: Int): String {
        val mask = "_"
        if (answer.isEmpty() || open <= 0) {
            return answer
        }
        var result = ""
        val words = answer.split(WHITESPACE_RX)
        words.forEach {
            val wordMask: Array<String?> = arrayOfNulls(it.length)
            for ((index, c) in it.withIndex()) {
                if (PUNCTUATION_RX.matches("" + c) || index < open) {
                    wordMask[index] = "" + c
                } else {
                    wordMask[index] = mask
                }
            }
            val fixedWord = wordMask.joinToString("").replace(Regex(mask), mask + " ").trim()
            result = result.plus("  ").plus(fixedWord)
        }
        return result.trim()
    }
}
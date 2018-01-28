package tk.germanbot.service

import org.springframework.stereotype.Service

@Service
class HintService {
    /**
     * Create a hint from current value
     * (Opens as many symbols in each word as the second parameter says)
     */
    fun hint(answer: String, open: Int, mask: String = "_ "): String {
        if (answer.isEmpty() || open <= 0) {
            return answer
        }
        var result = ""
        val words = answer.split(Regex("\\s+"))
        words.forEach {
            val wordMask: Array<String?> = arrayOfNulls(it.length)
            wordMask.fill(mask)
            for ((index, c) in it.withIndex()) {
                if (index >= open) {
                    break
                }
                wordMask[index] = "" + c
            }
            result = result.plus("   ").plus(wordMask.joinToString("").trim())
        }
        return result.trim()
    }
}
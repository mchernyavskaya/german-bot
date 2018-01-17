package tk.germanbot.service

import org.springframework.stereotype.Service

@Service
class HintService {
    /**
     * Create a hint from current value
     * (Opens as many symbols as the second parameter says)
     */
    fun hint(answer: String, open: Int): String {
        if (answer.isEmpty() || open <= 0) {
            return answer
        }
        val result: Array<Char?> = arrayOfNulls(answer.length)
        result.fill('-')
        for ((index, c) in answer.withIndex()) {
            if (index >= open) {
                break
            }
            result[index] = c
        }
        return result.joinToString("")
    }

}
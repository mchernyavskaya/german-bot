package tk.germanbot.service

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class HintServiceTest {
    val service: HintService = HintService()

    @Test
    fun hintOne() {
        val hint = service.hint("Answer", 1)
        assertThat(hint).isEqualTo("A*****")
    }

    @Test
    fun hintTwo() {
        val hint = service.hint("Answer", 2)
        assertThat(hint).isEqualTo("An****")
    }

    @Test
    fun hintTen() {
        val hint = service.hint("Answer", 10)
        assertThat(hint).isEqualTo("Answer")
    }

    @Test
    fun hintZero() {
        val hint = service.hint("Answer", 0)
        assertThat(hint).isEqualTo("Answer")
    }
}
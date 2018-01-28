package tk.germanbot.service

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class HintServiceTest {
    val service: HintService = HintService()

    @Test
    fun hintOne() {
        val hint = service.hint("Answer", 1)
        assertThat(hint).isEqualTo("A_ _ _ _ _")
    }

    @Test
    fun hintOneTwoWords() {
        val hint = service.hint("Answer Two", 1)
        assertThat(hint).isEqualTo("A_ _ _ _ _ T_ _")
    }

    @Test
    fun hintTwo() {
        val hint = service.hint("Answer", 2)
        assertThat(hint).isEqualTo("An_ _ _ _")
    }

    @Test
    fun hintTwoTwoWords() {
        val hint = service.hint("Answer Two", 2)
        assertThat(hint).isEqualTo("An_ _ _ _ Tw_")
    }

    @Test
    fun hintFourTwoWords() {
        val hint = service.hint("Answer Two", 4)
        assertThat(hint).isEqualTo("Answ_ _ Two")
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
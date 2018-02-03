package tk.germanbot.service

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import tk.germanbot.data.Quiz


class QuizTextFileGeneratorTest {

    @Test
    fun canGenerateFile() {

        val g = QuizTextFileGenerator()
        val result = g.generateFile(listOf(
                Quiz(createdBy = "me", question = "Q1", answers = setOf("A1", "A2"), topics = setOf("T2", "T1"), example = "this is example"),
                Quiz(id = "12345", isPublished = true, createdBy = "me", question = "Q2", answers = setOf("A21", "A22"), topics = setOf("T21", "T22"))
        ))

        assertThat(result.trim()).isEqualToIgnoringCase(
"""

Q1
A1
A2
#T1 #T2
E:this is example

ID:12345
Q2
A21
A22
#T21 #T22
!Published

""".trim()
        )
    }

}
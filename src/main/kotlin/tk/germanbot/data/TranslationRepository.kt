package tk.germanbot.data

import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface TranslationRepository : CrudRepository<Translation, String> {
    fun findOneById(id: String): Translation

    override fun findAll(): List<Translation>
    fun findByGe(ge: String): List<Translation>
    fun findByEn(en: String): List<Translation>
}
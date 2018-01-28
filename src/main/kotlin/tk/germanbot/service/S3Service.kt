package tk.germanbot.service

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class S3Service {

    private var bucketName : String? = "german-bot/temp"

    fun uploadFile(fileName: String, content: String) : String {
        val s3client = AmazonS3ClientBuilder.defaultClient()

        val bytes = content.toByteArray(charset(StandardCharsets.UTF_8.name()))

        val metadata = ObjectMetadata()
        metadata.contentLength = bytes.size.toLong()
        metadata.contentType = "text/plain"
        metadata.expirationTime = Date.from(Instant.now().plus(1, ChronoUnit.DAYS))

        ByteArrayInputStream(bytes).use { stream ->
            s3client.putObject(PutObjectRequest(bucketName, fileName, stream, metadata).withCannedAcl(CannedAccessControlList.PublicRead))
            return s3client.getUrl(bucketName, fileName).toString()
        }
    }

}
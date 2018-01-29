package tk.germanbot

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DynamoTools(
        @Autowired val props: AwsProperties
) {

    fun createTableIfNotExist(dbMapper: DynamoDBMapper, dynamoDB: AmazonDynamoDB, tableClass: Class<out Any>, tableName: String) {
        val tableRequest = dbMapper.generateCreateTableRequest(tableClass)
        tableRequest.provisionedThroughput = ProvisionedThroughput(props.readThroughput, props.writeThroughput)
        tableRequest.globalSecondaryIndexes?.forEach { i -> i.provisionedThroughput = tableRequest.provisionedThroughput }
        TableUtils.createTableIfNotExists(dynamoDB, tableRequest)
        TableUtils.waitUntilActive(dynamoDB, tableName)
    }

}
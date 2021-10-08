package networking

import org.json.JSONObject
import publisher.MESSAGE_TYPE

object Converter {

    fun createUpdateSubscriberMessage(metaData: MutableSet<MutableMap.MutableEntry<String, String?>>) : JSONObject {
        val subscriberUpdateMessage = JSONObject(utils.createJsonForGivenType(MESSAGE_TYPE.SUBSCRIBER, ""))
        val metaDataJson = JSONObject("{}")
        metaData.forEach {
            metaDataJson.put(it.key, it.value ?: JSONObject.NULL)
        }
        subscriberUpdateMessage.put("content", metaDataJson)
        return subscriberUpdateMessage
    }
}
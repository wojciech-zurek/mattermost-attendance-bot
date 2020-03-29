package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper

class PostDeserializer : JsonDeserializer<Post>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Post {
        val text = p.text
        val mapper = p.codec as ObjectMapper
        return mapper.readValue(text, Post::class.java)
    }
}
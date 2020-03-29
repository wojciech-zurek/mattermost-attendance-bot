package eu.wojciechzurek.mattermost.attendancebot.api.mattermost

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientException

class MattermostException(
        val httpStatus: HttpStatus,
        message: String
) : WebClientException(message)
package eu.wojciechzurek.mattermost.attendancebot.services

import eu.wojciechzurek.mattermost.attendancebot.AccessValidationResult
import eu.wojciechzurek.mattermost.attendancebot.api.mattermost.Event

interface AccessService {

    fun checkAccess(commandName: String, event: Event): AccessValidationResult
}
package net.snet.crm.service.resources.auth

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class CredentialsTest extends Specification {
    def 'should create credentials'() {
        def json = '{ "sessionId": "1234567" }'
        def mapper = new ObjectMapper()
        def credentials = mapper.readValue(json, Credentials)
        expect: credentials.getSessionId() == '1234567'
    }
}

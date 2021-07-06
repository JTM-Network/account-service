package com.jtm.account.core.util

import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import org.json.JSONArray
import org.json.JSONObject

class MailjetRequestBuilder(private val request: MailjetRequest = MailjetRequest(Emailv31.resource), private val jsonObject: JSONObject = JSONObject()) {

    fun withFrom(email: String, name: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.FROM, JSONObject()
            .put("Email", email)
            .put("Name", name))
        return this
    }

    fun withTo(email: String, name: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.TO, JSONArray()
            .put(JSONObject()
                .put("Email", email)
                .put("Name", name)))
        return this
    }

    fun withTo(email: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.TO, JSONArray()
            .put(JSONObject()
                .put("Email", email)))
        return this
    }

    fun withSubject(subject: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.SUBJECT, subject)
        return this
    }

    fun withText(text: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.TEXTPART, text)
        return this
    }

    fun withHtml(html: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.HTMLPART, html)
        return this
    }

    fun withCustomId(id: String): MailjetRequestBuilder {
        jsonObject.put(Emailv31.Message.CUSTOMID, id)
        return this
    }

    fun build(): MailjetRequest {
        request.property(Emailv31.MESSAGES, JSONArray().put(jsonObject))
        return request
    }
}
package de.dani09.http

import org.apache.commons.io.IOUtils
import java.net.HttpURLConnection
import java.net.URL

/**
 * With this class you can make Http requests to an Server
 * to do that instance it add some Header etc. depending on what you need
 * and then call the execute method to actually execute it
 *
 * You can make HttpRequests in an Builder like pattern
 * because each method is returning the current instance
 */
@Suppress("unused")
class HttpRequest(private val url: String,
                  private var httpMethod: HttpMethod) {

    private var requestHeaders: MutableMap<String, String> = mutableMapOf()
    private var userAgent: String = "Mozilla/5.0"

    /**
     * sets the UserAgent for the Request
     */
    fun setUserAgent(userAgent: String): HttpRequest {
        this.userAgent = userAgent
        return this
    }

    /**
     * adds an Request Header to the Request
     */
    fun addRequestHeader(headerName: String, headerValue: String): HttpRequest {
        requestHeaders[headerName] = headerValue
        return this
    }

    /**
     * executes the Http Request and returns the Response
     */
    fun execute() = Executor(this).executeHttpRequest()

    private class Executor(private val request: HttpRequest) {
        fun executeHttpRequest(): HttpResponse {
            val result: HttpResponse?
            val connection: HttpURLConnection = URL(request.url).openConnection() as HttpURLConnection

            try {
                // Adding props to connection
                connection.requestMethod = request.httpMethod.toString()
                addRequestHeaders(connection)

                // Creating Response from connection
                result = HttpResponse(
                        responseCode = connection.responseCode,
                        response = IOUtils.toByteArray(connection.inputStream),
                        responseHeaders = processResponseHeaders(connection)
                )
            } catch (e: Exception) {
                throw e
            } finally {
                connection.disconnect()
            }

            return result!!
        }

        private fun addRequestHeaders(c: HttpURLConnection) {
            c.setRequestProperty("User-Agent", request.userAgent)

            for ((k, v) in request.requestHeaders) {
                c.setRequestProperty(k, v)
            }
        }

        private fun processResponseHeaders(connection: HttpURLConnection): Map<String, String> {
            return connection.headerFields
                    .filter { it.key != null }
                    .filter { it.value != null && it.value.count { it == null } == 0 }
                    .filter { it.value.size > 0 }
                    .map { it.key to it.value.reduce { acc, s -> acc + s } }
                    .toMap()
        }
    }
}


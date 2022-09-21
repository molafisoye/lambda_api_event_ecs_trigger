package com.gex

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse, S3Event}
import org.apache.commons.logging.LogFactory

object Entry extends App {
    val logger = LogFactory.getLog(Entry.getClass.getName)

    logger.info("starting app")

    def handler(event: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse = {
        handleWebCall(event)
    }

    private def handleWebCall(event: APIGatewayV2HTTPEvent) = {
        println(s"Received body - ${event.toString}")

        val path = event.getRawPath.substring(1)
        val pipelineFilePrefix = path.substring(path.indexOf("/") + 1)

        if (!pipelineFilePrefix.isBlank) {
            EcsTaskHandler.createEnvFileInS3(inputFileName = pipelineFilePrefix)
        }
        val response = EcsTaskHandler.triggerTask(path.substring(0, path.indexOf("/")))

        APIGatewayV2HTTPResponse.builder()
                .withStatusCode(response.code)
                .withBody(response.text)
                .build()
    }


}


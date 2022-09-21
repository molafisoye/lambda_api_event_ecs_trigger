package com.gex

import com.amazonaws.ClientConfiguration
import com.amazonaws.services.ecs.model.{AssignPublicIp, AwsVpcConfiguration, NetworkConfiguration, RunTaskRequest}
import com.amazonaws.services.ecs.{AmazonECS, AmazonECSClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

object EcsTaskHandler {
    val client: AmazonECS = AmazonECSClientBuilder.standard.withClientConfiguration(new ClientConfiguration()).build()
    val clusterName: String = Config.clusterName
    val subnet: String = Config.subnet
    val securityGroup: String = Config.securityGroup

    def triggerTask(taskName: String): Response = {

        println(s"running task - $taskName")

        val vpcConfiguration: AwsVpcConfiguration = new AwsVpcConfiguration()
                .withSubnets(subnet)
                .withAssignPublicIp(AssignPublicIp.ENABLED)
                .withSecurityGroups(securityGroup)
                .withAssignPublicIp(AssignPublicIp.ENABLED)
        val networkConfiguration: NetworkConfiguration = new NetworkConfiguration()
        networkConfiguration.setAwsvpcConfiguration(vpcConfiguration)

        val runTaskRequest = new RunTaskRequest()
        runTaskRequest.setCluster(clusterName)
        runTaskRequest.setLaunchType("FARGATE")
        runTaskRequest.setNetworkConfiguration(networkConfiguration)

        taskName.toLowerCase() match {
            case "umitools_extract" =>
                runTaskRequest.setTaskDefinition(Config.umitoolsExtractTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "umitools_dedup" =>
                runTaskRequest.setTaskDefinition(Config.umitoolsDedupTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "htseq_count" =>
                runTaskRequest.setTaskDefinition(Config.htseqCountTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "samtools_index" =>
                runTaskRequest.setTaskDefinition(Config.samtoolsIndexTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "bbduk" =>
                runTaskRequest.setTaskDefinition(Config.bbdukTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "star" =>
                runTaskRequest.setTaskDefinition(Config.starTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case "fastqc" =>
                runTaskRequest.setTaskDefinition(Config.fastqcTaskDefFamily)
                runTask(runTaskRequest)
                Response()
            case _ => Response(s"unable to find task $taskName. Valid tasks are [umitools_extract, umitools_dedup, htseq_count, samtools_index, bbduk, star, fastqc]", 404)
        }
    }

    def runTask(request: RunTaskRequest): Unit = {
        client.runTask(request)
    }

    def createEnvFileInS3(envFileName: String = "env_file.env", inputFileName: String): Unit = {
        println(s"writing $envFileName with FILENAME $inputFileName")
        val s3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
        s3Client.putObject("gex-fargate-bucket", s"fargate-inputs/$envFileName", s"FILENAME=$inputFileName")
    }



    case class Response(text: String = "okay", code: Int = 200)
}

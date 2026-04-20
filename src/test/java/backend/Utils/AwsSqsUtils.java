package backend.Utils;
import hooks.BaseTest;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class AwsSqsUtils extends BaseTest {

    private static final String QUEUE_URL = "https://sqs.ap-south-1.amazonaws.com/841515273180/customerconsent-ap-south-1";

    public static String pushMessageToQueue(String messageBody) {
        // Initializes client using local ~/.aws/credentials
        try (SqsClient sqsClient = SqsClient.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create())
                .build())  {

            log.info("Pushing consent payload to SQS: {}", QUEUE_URL);
            log.info("Request Body: \n{}", messageBody);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMsgRequest);
            log.info("Message sent successfully! Message ID: {}", response.messageId());

            return response.messageId();
        } catch (SqsException e) {
            log.error("AWS SQS Error: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send message to SQS", e);
        }
    }

}

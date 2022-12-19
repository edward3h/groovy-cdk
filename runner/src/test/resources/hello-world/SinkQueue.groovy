import groovy.transform.builder.Builder
import groovy.util.logging.Log
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription
import software.amazon.awscdk.services.sqs.Queue
import software.amazon.awscdk.services.sqs.QueueProps
import software.constructs.Construct

@Log
class SinkQueue extends Construct {
    Queue queue
    int expectedTopicCount
    private int actualTopicCount

    SinkQueue(Construct parent, String name, SinkQueueProps props) {
        super(parent, name)
        props ?= SinkQueueProps.builder().build()
        var queueProps = props.queueProps ?: QueueProps.builder().build()
        expectedTopicCount = props.requiredTopicCount?.intValue() ?: 0
        queue = new Queue(this, "Resource", queueProps)
        node.addValidation {
            if (actualTopicCount < expectedTopicCount) {
                return ["There are not enough subscribers to the sink. Expecting $expectedTopicCount, actual is $actualTopicCount" as String]
            }
            return []
        }
    }

    void subscribe(Topic... topics) {
        topics.each {topic ->
            log.info "Subscribe $topic"
            if (expectedTopicCount != 0 && actualTopicCount >= expectedTopicCount) {
                throw new RuntimeException(
                        "Cannot add more topics to the sink. Maximum topics is configured to $expectedTopicCount")
            }
            topic.addSubscription(new SqsSubscription(queue))
            actualTopicCount++
        }
    }
}

@Builder
class SinkQueueProps {
    QueueProps queueProps
    Number requiredTopicCount
}
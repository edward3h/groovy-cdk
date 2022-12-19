stack HelloJavaStack {
    vpc = create(Vpc, "VPC")


    topicCount = 5

    sinkQueue = create(SinkQueue, "MySinkQueue") {
        requiredTopicCount 5
    }
    (1..topicCount).each {
        def topic = create(Topic, "Topic$it")
        sinkQueue.subscribe(topic)
    }


    create(MyAutoScalingGroup) {
        vpc vpc
    }
}

construct MyAutoScalingGroup, vpc:Vpc {
    create(AutoScalingGroup, "Compute") {
        instanceType(new InstanceType("t2.micro"))
        machineImage(new AmazonLinuxImage())
        vpc props.vpc
    }
}
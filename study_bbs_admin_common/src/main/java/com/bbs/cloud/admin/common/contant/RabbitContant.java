package com.bbs.cloud.admin.common.contant;

/**
 * RabbitMQ的一些定义：用于消息通信（生产者、消费者、交换机、队列）
 */
public class RabbitContant {

    /**
     * TEST组件需要定义的配置--------------start
     */
    public static final String TEST_EXCHANGE_NAME = "test_topic_exchange";//交换机

    public static final String TEST_QUEUE_NAME = "test_queue";//队列

    public static final String TEST_ROUTING_KEY = "test_rounting";//路由

    /**
     * TEST组件需要定义的配置--------------end
     */

    /**
     * SERVICE组件需要定义的配置--------------start
     */
    public static final String SERVICE_EXCHANGE_NAME = "service_topic_exchange";

    public static final String SERVICE_QUEUE_NAME = "service_queue";

    public static final String SERVICE_ROUTING_KEY = "service_rounting";

    /**
     * SERVICE组件需要定义的配置--------------end
     */

    /**
     * ACTIVITY组件需要定义的配置--------------start
     */
    public static final String ACTIVITY_EXCHANGE_NAME = "activity_topic_exchange";

    public static final String ACTIVITY_QUEUE_NAME = "activity_queue";

    public static final String ACTIVITY_ROUTING_KEY = "activity_rounting";
    /**
     * ACTIVITY组件需要定义的配置--------------end
     */

}

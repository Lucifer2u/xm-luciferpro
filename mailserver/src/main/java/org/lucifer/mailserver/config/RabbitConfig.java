package org.lucifer.mailserver.config;

import org.lucifer.vbluciferpro.model.MailConstants;
import org.lucifer.vbluciferpro.service.MailSendLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public final static Logger logger = LoggerFactory.getLogger(RabbitConfig.class);

    @Autowired
    CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    MailSendLogService mailSendLogService;

    @Bean
    RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setConfirmCallback((data, ack, cause) -> {
            String msgId = data.getId();
            if (ack) {
                logger.info(msgId + ":消息发送成功");
                mailSendLogService.updateMailSendLogStatus(msgId, 1);//修改数据库中的记录，消息投递成功
            } else {
                logger.info(msgId + ":消息发送失败");
            }
        });
        rabbitTemplate.setReturnCallback((msg, repCode, repText, exchange, routingkey) -> {
            logger.info("消息发送失败");
        });
        return rabbitTemplate;
    }

    /*原先队列绑定
    @Bean
    Queue mailQueue() {
        return new Queue(MailConstants.MAIL_QUEUE_NAME, true);
    }

    @Bean
    DirectExchange mailExchange() {
        return new DirectExchange(MailConstants.MAIL_EXCHANGE_NAME, true, false);
    }

    @Bean
    Binding mailBinding() {
        return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(MailConstants.MAIL_ROUTING_KEY_NAME);
    }*/


    //死信队列绑定
    @Bean
    public Queue getDeadQueue() {
        return new Queue(MailConstants.DEAD_MAIL_QUEUE_NAME);
    }
    @Bean
    public Exchange getDeadExchange() {
        return ExchangeBuilder.directExchange(MailConstants.DEAD_MAIL_EXCHANGE_NAME).durable(true).build();
    }
    //死信队列与死信交换机进行绑定
    @Bean
    public Binding bindDead() {
        return BindingBuilder.bind(getDeadQueue()).to(getDeadExchange()).with(MailConstants.DEAD_MAIL_ROUTING_KEY_NAME).noargs();
    }
    //创建工作队列
    @Bean
    public Queue getNormalQueue() {
        Map args = new HashMap();
        //当消息发送异常的时候，消息需要路由到的交换机和routing-key，这里配的直接是发送至死信队列
        args.put("x-dead-letter-exchange", MailConstants.DEAD_MAIL_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", MailConstants.DEAD_MAIL_ROUTING_KEY_NAME);
        //创建队列的时候，将死信绑定到队列中
        return QueueBuilder.durable(MailConstants.MAIL_QUEUE_NAME).withArguments(args).build();
    }
    //创建工作交换机
    @Bean
    public Exchange getNormalExchange() {
        return ExchangeBuilder.directExchange(MailConstants.MAIL_EXCHANGE_NAME).durable(true).build();
    }
    //工作队列与工作交换机进行绑定
    @Bean
    public Binding bindNormal() {
        return BindingBuilder.bind(getNormalQueue()).to(getNormalExchange()).with(MailConstants.MAIL_ROUTING_KEY_NAME).noargs();
    }


}

package org.lucifer.mailserver.receiver;

import com.rabbitmq.client.Channel;

import org.lucifer.vbluciferpro.model.Employee;
import org.lucifer.vbluciferpro.model.MailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;


//监听死信队列中的消息
@Component
public class DeadQueueMailReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailReceiver.class);

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private MailProperties mailProperties;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;


    @RabbitListener(queues = MailConstants.DEAD_MAIL_QUEUE_NAME)
    public void handleDeadQueue(Message message, Channel channel) {
        //获取员工类
        Employee employee = (Employee) message.getPayload();
        MessageHeaders headers = message.getHeaders();
        //获取消息序号
        long tag = (long) headers.get(AmqpHeaders.DELIVERY_TAG);
        //获取消息的msgId
        String msgId = (String) headers.get("spring_returned_message_correlation");
        HashOperations hashOperations = redisTemplate.opsForHash();
        try {
            //判断redis中是否存在msgId，如果有，直接返回
            if (hashOperations.entries("mail_log").containsKey(msgId)) {
                LOGGER.error("死信队列中的消息已经被消费========>", msgId);
                //手动确认消息
                channel.basicAck(tag, false);
                return;
            }
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg);
            //设置发送人
            messageHelper.setFrom(mailProperties.getUsername());
            //设置收件人
            messageHelper.setTo(employee.getEmail());
            //设置发送主体
            messageHelper.setSubject("入职欢迎邮件");
            //设置发送日期
            messageHelper.setSentDate(new Date());
            //设置邮件内容
            Context context = new Context();
            //如下参数对应mail.html中模板引擎的参数
            context.setVariable("name", employee.getName());
            context.setVariable("posName", employee.getPosition().getName());
            context.setVariable("joblevelName", employee.getJobLevel().getName());
            context.setVariable("departmentName", employee.getDepartment().getName());
            String mail = templateEngine.process("mail", context);
            messageHelper.setText(mail, true); //参数1：邮件参数 参数2：是否是html邮件
            //发送邮件
            javaMailSender.send(msg);
            LOGGER.info("邮件重发成功 ========>");
            //邮件发送成功后，将msgId标识存入redis
            hashOperations.put("mail_log", msgId, "ok");
            channel.basicAck(tag, false);
            /**
             * 手动确认消息，拒绝接收到的消息，退回到队列，也就是说如果消息的消费出现异常，会将消息退回到队列中
             * @tag 消息序号
             * @multiple 是否处理多条
             * @requeue 是否要退回到队列，如果是false，消息不会重发，会把消息打入死信队列。如果是true，会无限次重试导致死循环，不建议加try-catch
             */
        } catch (Exception e) {
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception exception) {
                if (exception instanceof MailSendException){
                    LOGGER.error("未找到该邮箱============>" + e.getMessage());
                }
                exception.printStackTrace();
            }
            LOGGER.error("邮件第二次发送失败 ========>", e.getMessage());
        }
    }
}

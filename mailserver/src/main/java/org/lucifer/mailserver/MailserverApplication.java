package org.lucifer.mailserver;

import org.lucifer.vbluciferpro.model.MailConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


/**
 * @author lucifer
 */
@SpringBootApplication
public class MailserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailserverApplication.class, args);
    }

    @Bean
    Queue queue(){
        return new Queue(MailConstants.MAIL_QUEUE_NAME);
    }
}

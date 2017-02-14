package uk.gov.ons.ctp.response.kirona.drs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The 'main' entry point into the Kirona Gateway SpringBoot Application.
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableAsync
@EnableCaching
@EnableScheduling
@ImportResource("context.xml")
@Slf4j
public class Application {

  /**
   * This method is the entry point to the Spring Boot application.
   *
   * @param args These are the optional command line arguments
   */
  public static void main(final String[] args) throws InterruptedException {
    log.debug("About to start Application with philippe.brossierAAA@ons.gov.uk...");
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    MessageChannel messageChannel = context.getBean("pollableChannel", MessageChannel.class);
    messageChannel.send(new GenericMessage<>("myTestString"));
    Thread.sleep(10000);
    context.close();
  }

  @ServiceActivator(inputChannel = "pollableChannel", poller = @Poller(fixedDelay = "5000"))
  public void foo(String in) {
    log.debug("reading the String from the queue {}...", in);
  }
}

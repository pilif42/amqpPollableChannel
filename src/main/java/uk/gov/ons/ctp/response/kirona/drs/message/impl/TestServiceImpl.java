package uk.gov.ons.ctp.response.kirona.drs.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.kirona.drs.message.TestService;

@Slf4j
//@MessageEndpoint
public class TestServiceImpl implements TestService {

//  @ServiceActivator(inputChannel = "pollableChannel", poller = @Poller(value = "customPoller"))
  public final void processString(final String inputStr) {
    log.debug("zzz I can see I come here with in = {}", inputStr);
  }
}

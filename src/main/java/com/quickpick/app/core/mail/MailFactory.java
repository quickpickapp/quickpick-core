package com.quickpick.app.core.mail;

import com.quickpick.app.core.log.Log;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MailFactory {
  private final Log log;
  private final OutgoingMailRepository outgoingMailDatabaseTable;
  private final MailTemplate mailTemplate;
  private final AbstractConfiguration file;

  public Mail create(String name) {
    var configuration = MailConfiguration.create(name);
    configuration.load(file);
    return create(configuration.mail(), configuration.smtpHost(),
      configuration.smtpPort(), configuration.imapHost(),
      configuration.imapPort(), configuration.user(), configuration.password());
  }

  public Mail create(
    String mail, String smtpHost, int smtpPort, String imapHost, int imapPort,
    String user, String password
  ) {
    return Mail.create(log, outgoingMailDatabaseTable, mailTemplate, mail,
      smtpHost, smtpPort, imapHost, imapPort, user, password);
  }
}

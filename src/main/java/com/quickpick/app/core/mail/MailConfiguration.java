package com.quickpick.app.core.mail;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.AbstractConfiguration;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class MailConfiguration implements Configuration {
  private final String name;
  private String mail;
  private String smtpHost;
  private int smtpPort;
  private String imapHost;
  private int imapPort;
  private String user;
  private String password;

  @Override
  public void load(AbstractConfiguration file) {
    mail = file.getString(name + ".mail");
    smtpHost = file.getString(name + ".smpt_host");
    smtpPort = file.getInt(name + ".smpt_port");
    imapHost = file.getString(name + ".imap_host");
    imapPort = file.getInt(name + ".imap_port");
    user = file.getString(name + ".user");
    password = file.getString(name + ".password");
  }
}
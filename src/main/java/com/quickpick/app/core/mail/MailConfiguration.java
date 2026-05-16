package com.quickpick.app.core.mail;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.INIConfiguration;

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
  public void load(INIConfiguration file) {
    var section = "mail." + name;
    mail = file.getSection(section).getString("mail");
    smtpHost = file.getSection(section).getString("smtp_host");
    smtpPort = file.getSection(section).getInt("smtp_port");
    imapHost = file.getSection(section).getString("imap_host");
    imapPort = file.getSection(section).getInt("imap_port");
    user = file.getSection(section).getString("user");
    password = file.getSection(section).getString("password");
  }
}
package com.quickpick.app.core.mail;

import com.google.common.collect.Lists;
import com.quickpick.app.core.log.Log;
import com.quickpick.app.core.user.User;
import lombok.RequiredArgsConstructor;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@RequiredArgsConstructor(staticName = "create")
public class Mail {
  private final Log log;
  private final OutgoingMailRepository outgoingMailDatabaseTable;
  private final MailTemplate template;
  private final String mail;
  private final String smtpHost;
  private final int smtpPort;
  private final String imapHost;
  private final int imapPort;
  private final String user;
  private final String password;

  public CompletableFuture<List<MailMessage>> inbox() {
    var futureResponse = new CompletableFuture<List<MailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(false))).start();
    return futureResponse;
  }

  public CompletableFuture<List<MailMessage>> inboxAndFlush() {
    var futureResponse = new CompletableFuture<List<MailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(true))).start();
    return futureResponse;
  }

  private List<MailMessage> readInbox(boolean delete) {
    try {
      var session = createSession("imap", imapHost, imapPort);
      var store = session.getStore("imap");
      store.connect(imapHost, user, password);
      var folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);
      if (delete) {
        deleteMessages(folder.getMessages());
      }
      var messages = Arrays.stream(folder.getMessages())
        .map(MailMessage::of).toList();
      folder.close(true);
      store.close();
      return messages;
    } catch (Exception exception) {
      log.processError(exception);
      return null;
    }
  }

  private void deleteMessages(Message[] messages) throws Exception {
    for (var i = 0; i < messages.length; i++) {
      messages[i].setFlag(Flags.Flag.DELETED, true);
    }
  }

  public CompletableFuture<String> send(User user, String title, String body) {
    return send(user.email(), title, body);
  }

  public CompletableFuture<String> send(
    String target, String title, String body
  ) {
    return send(target, title, body, Lists.newArrayList());
  }

  public CompletableFuture<String> send(
    User user, String title, String body, List<MailAttachment> attachments
  ) {
    return send(user.email(), title, body, attachments);
  }

  public CompletableFuture<String> send(
    String target, String title, String body,
    List<MailAttachment> attachments
  ) {
    var futureResponse = new CompletableFuture<String>();
    new Thread(() -> sendEmail(target, title, body, attachments,
      futureResponse)).start();
    return futureResponse;
  }

  private void sendEmail(
    String target, String title, String body,
    List<MailAttachment> attachments, CompletableFuture<String> futureResponse
  ) {
    try {
      var session = createSession("smtp", smtpHost, smtpPort);
      var message = createMessage(session, new Address[] {createAddress(target)},
        title, body, attachments);
      var transport = session.getTransport("smtp");
      transport.connect(smtpHost, user, password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      var messageId = message.getHeader("Message-ID")[0];
      outgoingMailDatabaseTable.generateAvailableId(UUID::randomUUID)
        .thenAccept(id -> outgoingMailDatabaseTable.save(
          OutgoingMail.create(id, target, mail, System.currentTimeMillis(),
            title, serializeMessage(message)))
          .thenAccept(_ -> futureResponse.complete(messageId)));
    } catch (Exception exception) {
      log.processError(exception);
    }
  }

  private InternetAddress createAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (Exception exception) {
      log.processError(exception);
      return null;
    }
  }

  private Session createSession(String protocol, String host, int port) {
    var properties = System.getProperties();
    properties.put("mail." + protocol + ".host", host);
    properties.put("mail." + protocol + ".port", port);
    properties.put("mail." + protocol + ".starttls.enable", "true");
    properties.put("mail." + protocol + ".socketFactory.class",
      "javax.net.ssl.SSLSocketFactory");
    var session = Session.getDefaultInstance(properties, null);
    session.setDebug(false);
    return session;
  }

  private MimeMessage createMessage(
    Session session, Address[] addresses, String title,
    String body, List<MailAttachment> attachments
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Dulno"));
    message.setRecipients(Message.RecipientType.TO, addresses);
    message.setSentDate(new Date());
    message.setSubject(title);
    var content = buildMessageContent(body);
    if (attachments.isEmpty()) {
      message.setContent(content, "text/html; charset=utf-8");
    } else {
      message.setContent(createMultipartBody(content, attachments));
    }
    return message;
  }

  private String buildMessageContent(String body) {
    return template.mailTemplate()
      .replaceAll("%YEAR%", String.valueOf(Year.now().getValue()))
      .replaceAll("%CONTENT%", formatMailBodyLinks(body).replaceAll("\n", "<br>"));
  }

  private static final Pattern MAIL_LINK_PATTERN =
    Pattern.compile("(https?://[a-zA-Z0-9\\-._~:/?#@!$&'()*+,;=%]+)");

  private String formatMailBodyLinks(String text) {
    var matcher = MAIL_LINK_PATTERN.matcher(text);
    var result = new StringBuilder();
    while (matcher.find()) {
      var url = matcher.group(1);
      var replacement = "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
      matcher.appendReplacement(result, replacement);
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private MimeMultipart createMultipartBody(
    String content, List<MailAttachment> attachments
  ) throws Exception {
    var multipart = new MimeMultipart();
    var textBodyPart = new MimeBodyPart();
    textBodyPart.setContent(content, "text/html; charset=utf-8");
    multipart.addBodyPart(textBodyPart);
    for (var attachment : attachments) {
      addAttachmentPart(attachment, multipart);
    }
    return multipart;
  }

  private void addAttachmentPart(
    MailAttachment attachment, MimeMultipart multipart
  ) throws Exception {
    var attachmentBodyPart = new MimeBodyPart();
    var source = new FileDataSource(attachment.file().getAbsolutePath());
    attachmentBodyPart.setDataHandler(new DataHandler(source));
    attachmentBodyPart.setFileName(attachment.name());
    multipart.addBodyPart(attachmentBodyPart);
  }

  private byte[] serializeMessage(MimeMessage message) {
    try {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      message.writeTo(byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
    } catch (Exception exception) {
      log.processError(exception);
      return null;
    }
  }

  public void resendEmail(OutgoingMail outgoingMail) {
    try {
      var session = createSession("smtp", smtpHost, smtpPort);
      var message = deserializeMessage(session, outgoingMail.content());
      message.setSentDate(new Date());
      var transport = session.getTransport("smtp");
      transport.connect(smtpHost, user, password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      outgoingMailDatabaseTable.generateAvailableId(UUID::randomUUID)
        .thenAccept(id -> outgoingMailDatabaseTable.save(
          OutgoingMail.create(id, outgoingMail.receiver(), mail,
            System.currentTimeMillis(), outgoingMail.title(),
            serializeMessage(message))));
    } catch (Exception exception) {
      log.processError(exception);
    }
  }

  private MimeMessage deserializeMessage(Session session, byte[] content) {
    try {
      var byteArrayInputStream = new ByteArrayInputStream(content);
      return new MimeMessage(session, byteArrayInputStream);
    } catch (Exception exception) {
      log.processError(exception);
      return null;
    }
  }
}

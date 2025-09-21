package com.emailreader.util;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;

import java.io.FileWriter;
import java.util.Properties;

public class GmailReaderPlainText {

    public static void main(String[] args) {
        String host = "imap.gmail.com";
        String username = "mahirciler@gmail.com";
        String appPassword = "knolooqipjuirpqb";

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try (FileWriter writer = new FileWriter("emails.txt")) {  // Save as plain text
            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                String body = getTextFromMessage(message).trim();
                if (!body.isEmpty()) {
                    writer.write(body + "\n\n######\n\n"); // separate each email
                }
            }

            inbox.close(false);
            store.close();

            System.out.println("All emails have been saved to emails.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Extract only readable text
    private static String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            // strip HTML tags
            return message.getContent().toString().replaceAll("<[^>]*>", "");
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/html")) {
                return bodyPart.getContent().toString().replaceAll("<[^>]*>", "");
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                return getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return "";
    }
}
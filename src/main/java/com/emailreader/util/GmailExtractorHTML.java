package com.emailreader.util;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;

import java.io.FileWriter;
import java.util.Properties;

public class GmailExtractorHTML {

    public static void main(String[] args) {
        String host = "imap.gmail.com";
        String username = "mahirciler@gmail.com";
        String appPassword = "knolooqipjuirpqb";

        // Words to extract content between
        String startWord = "updating";
        String endWord = "September";

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try (FileWriter writer = new FileWriter("extractedEmails.html")) {
            // HTML header
            writer.write("<html><body>\n");

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                if (message.getFrom()[0].toString().contains("no-reply@legal.spotify.com")){
                    String content = getHTMLContent(message);

                    // Extract between startWord and endWord
                    int startIndex = content.indexOf(startWord);
                    int endIndex = content.indexOf(endWord, startIndex + startWord.length());

                    if (startIndex != -1 && endIndex != -1) {
                        String extracted = content.substring(startIndex, endIndex + endWord.length());
                        writer.write("<div style='border:1px solid #ccc; margin:10px; padding:10px;'>\n");
                        writer.write(extracted + "\n");
                        writer.write("</div>\n");
                    }
                }
            }

            writer.write("</body></html>");
            inbox.close(false);
            store.close();

            System.out.println("Extraction done. Check extractedEmails.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get HTML content from message
    private static String getHTMLContent(Message message) throws Exception {
        if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/plain")) {
            // convert plain text to HTML format
            return "<pre>" + message.getContent().toString() + "</pre>";
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getHTMLFromMultipart(mimeMultipart);
        }
        return "";
    }

    private static String getHTMLFromMultipart(MimeMultipart mimeMultipart) throws Exception {
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                return getHTMLFromMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return "";
    }
}


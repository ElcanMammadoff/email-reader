package com.emailreader.util;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FromStringTerm;

import java.io.FileWriter;
import java.util.Properties;

public class GmailExtractPart {

    public static void main(String[] args) {
        String host = "imap.gmail.com";
        String username = "example@gmail.com"; //Todo here you must add your email.
        String appPassword = "hhhhkkkklllluuuu"; //Todo here you must get App passsword from your gmail security settings and add it to here.

        String startWord = "Today's hack"; //TODO here you give start part of email.(for example  you want to take from 'Hello' word to 'Bye' word.you give 'Hello' here.
        String endFragment = "<img alt=\"\" src=\"http://e.growthhackingidea.com/files/e.growthhackingidea.com/general/avatar.png\""; //TODO here you give end of part that you want to get.

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try (FileWriter writer = new FileWriter("extractedHacks.html")) { //TODO here you give file name that you want to save data there.
            writer.write("<html><body>\n");

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Only search emails from this sender
            FromStringTerm senderFilter = new FromStringTerm("admin@growthhackingidea.com");  //TODO here you give email who you want to get messages from. (for example you want to read only Charlie's email.You write charlie@gmail.com)
            Message[] messages = inbox.search(senderFilter);

            System.out.println("Emails found from sender: " + messages.length);
            int count = 0;

            for (Message message : messages) {
                try {
                    String content = getHTMLContent(message);

                    // Normalize line breaks and spaces for robustness
                    String normalized = content.replaceAll("\\s+", " ");

                    int startIndex = normalized.indexOf(startWord);
                    if (startIndex != -1) {
                        startIndex += startWord.length(); // exclude the start word

                        int endIndex = normalized.indexOf(endFragment, startIndex);
                        if (endIndex != -1) {
                            String extracted = normalized.substring(startIndex, endIndex).trim();

                            writer.write("<div style='border:1px solid #ccc; margin:10px; padding:10px;'>\n");
                            writer.write(extracted + "\n");
                            writer.write("</div>\n");

                            count++;
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error processing email: " + e.getMessage());
                }
            }

            writer.write("</body></html>");
            inbox.close(false);
            store.close();

            System.out.println("Extraction complete. Hacks extracted: " + count);
            System.out.println("Check extractedHacks.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getHTMLContent(Message message) throws Exception {
        if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/plain")) {
            return "<pre>" + message.getContent().toString() + "</pre>";
        } else if (message.isMimeType("multipart/*")) {
            return getMultipartContent((MimeMultipart) message.getContent());
        }
        return "";
    }

    private static String getMultipartContent(MimeMultipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.isMimeType("text/html")) {
                result.append(part.getContent().toString());
            } else if (part.isMimeType("text/plain")) {
                result.append("<pre>").append(part.getContent().toString()).append("</pre>");
            } else if (part.getContent() instanceof MimeMultipart) {
                result.append(getMultipartContent((MimeMultipart) part.getContent()));
            }
        }
        return result.toString();
    }
}
package com.emailreader.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;

import java.io.FileWriter;
import java.util.Properties;

public class GmailExtractorHTMLBatch {

    public static void main(String[] args) {

        String host = "imap.gmail.com";
        String username = "elmin.mehreliyev75@gmail.com";
        String appPassword = "";

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try (FileWriter writer = new FileWriter("extractedEmails.html")) {

            writer.write("<html><body>\n");

            Session session = Session.getDefaultInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Fetch all messages
            Message[] messages = inbox.getMessages();
            System.out.println("Total emails in inbox: " + messages.length);

            int extractedCount = 0;

            for (Message message : messages) {
                try {
                    // ✅ Filter emails reliably by email address only
                    Address[] fromAddresses = message.getFrom();
                    boolean isCharlie = false;
                    for (Address addr : fromAddresses) {
                        if (addr instanceof InternetAddress) {
                            InternetAddress iAddr = (InternetAddress) addr;
                            String emailOnly = iAddr.getAddress(); // admin@growthhackingidea.com
                            if ("admin@growthhackingidea.com".equalsIgnoreCase(emailOnly)) {
                                isCharlie = true;
                                break;
                            }
                        }
                    }
                    if (!isCharlie) continue;

                    String content = getHTMLContent(message);

                    // ✅ Flexible extraction: "Today's hack" → first image
                    int startIndex = content.indexOf("Today's hack");
                    if (startIndex != -1) {
                        int divClose = content.indexOf("</div>", startIndex);
                        if (divClose != -1) startIndex = divClose + 6; // move after div

                        int endIndex = content.indexOf("<img src=\"https://ci3.googleusercontent.com", startIndex);
                        if (endIndex != -1) {
                            int imgClose = content.indexOf(">", endIndex);
                            if (imgClose != -1) endIndex = imgClose + 1;

                            String extracted = content.substring(startIndex, endIndex);

                            writer.write("<div style='border:1px solid #ccc; margin:10px; padding:10px;'>\n");
                            writer.write(extracted + "\n");
                            writer.write("</div>\n");

                            extractedCount++;
                        }
                    }

                    // Small delay to avoid Gmail rate limits
                    Thread.sleep(200 + (int) (Math.random() * 200));

                } catch (Exception e) {
                    System.out.println("Error processing email: " + e.getMessage());
                }
            }

            writer.write("</body></html>");
            inbox.close(false);
            store.close();

            System.out.println("Extraction complete. Emails extracted: " + extractedCount);
            System.out.println("Check extractedEmails.html");

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
            return getHTMLFromMultipart((MimeMultipart) message.getContent());
        }
        return "";
    }

    private static String getHTMLFromMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart part = mimeMultipart.getBodyPart(i);
            if (part.isMimeType("text/html")) {
                result.append(part.getContent().toString());
            } else if (part.getContent() instanceof MimeMultipart) {
                result.append(getHTMLFromMultipart((MimeMultipart) part.getContent()));
            }
        }
        return result.toString();
    }
}

package com.vyn.motoclick.database;

import java.util.Comparator;

public class ContactData implements Comparator<ContactData> {
    String chatId;
    String contactId;
    boolean newMessage;

    String contactName;
    String contactToken;
    String contactUriPhoto;

    public ContactData() {
    }

    public ContactData(String chatId, String contactId, boolean newMessage) {
        this.chatId = chatId;
        this.contactId = contactId;
        this.newMessage = newMessage;
    }

    public ContactData(String chatId, String contactId, String contactName, String contactToken, String contactUriPhoto) {
        this.chatId = chatId;
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactToken = contactToken;
        this.contactUriPhoto = contactUriPhoto;
    }

    public String getChatId() {
        return chatId;
    }

    public String getContactId() {
        return contactId;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactToken() {
        return contactToken;
    }

    public String getContactUriPhoto() {
        return contactUriPhoto;
    }

    @Override
    public int compare(ContactData o1, ContactData o2) {
        return o1.getContactName().compareToIgnoreCase(o2.getContactName());
    }
}
package net;

import collection.CollectionElement;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class PacketMessage implements Serializable {
    public static enum Head implements Serializable {
        INFO,
        REMOVE_FIRST,
        REMOVE_FIRST_OK,
        REMOVE_FIRST_ERROR,
        REMOVE_LAST,
        REMOVE_LAST_OK,
        REMOVE_LAST_ERROR,
        ADD,
        ADD_OK,
        ADD_ERROR,
        REMOVE,
        REMOVE_OK,
        REMOVE_ERROR,
        SHOW,
        IMPORT,
        LOAD,
        SAVE,
        STOP,
        EMAIL_LOGIN,
        TOKEN_LOGIN,
        ERROR,
        LOGIN_OK,
        LOGIN_ERROR,
        SET_ADDRESS,
        EMAIL_OK,
        EMAIL_ERROR,
    }

    private Head head;
    private Object body;
    private Date creationDate = new Date();
    private String login;
    private String token;
    private SocketAddress address;
    private ArrayList<CollectionElement> elements;
    private String errorMessage;

    public PacketMessage(Head head) {
        this.head = head;
    }

    public PacketMessage(Head head, String errorMessage, boolean error) {
        this.head = head;
        this.errorMessage = errorMessage;
    }

    public PacketMessage(Head head, ArrayList<CollectionElement> elements) {
        this.head = head;
        this.elements = elements;
    }

    public PacketMessage(Head head, SocketAddress address) {
        this.head = head;
        this.address = address;
    }

    public PacketMessage(Head head, String token) {
        this.head = head;
        this.token = token;
    }

    public PacketMessage(Head head, Object body, String login, String token, SocketAddress address) {
        this.head = head;
        this.body = body;
        this.login = login;
        this.token = token;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("{head: %s; body: %s; creationDate: %s }",
                head, body, creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PacketMessage.class, head, body, creationDate);
    }

    public Head getHead() {
        return head;
    }

    public Object getBody() {
        return body;
    }

    public String getLogin() {
        return login;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getToken() { return token; }

    public SocketAddress getAddress() { return address; }

    public ArrayList<CollectionElement> getElements() { return elements; }

    public String getErrorMessage() { return errorMessage; }

    public void setAddress (SocketAddress address) {
        this.address = address;
    }
}

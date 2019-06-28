package net;

import collection.CollectionElement;
import db.Database;

import java.net.SocketAddress;
import java.util.ArrayList;

public class Shower implements Runnable {
    private Server server;
    private Database database;

    public void setDefaults(Server server, Database database) {
        this.server = server;
        this.database = database;
    }

    @Override
    public  void run() {
        while (true) {
            try {
                Thread.sleep(20000);
                ArrayList<CollectionElement> list = database.show(database.getUserId("", ""));
                list.sort(CollectionElement::compareTo);
                for (SocketAddress user : server.getUsers()) {
                    server.sendMessage(user, new PacketMessage(PacketMessage.Head.SHOW, list));
                }
            } catch (InterruptedException skip) {

            }
        }
    }
}

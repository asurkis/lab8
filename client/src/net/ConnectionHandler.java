package net;

import client.Main;

public class ConnectionHandler implements Runnable, Cloneable {
    private MessageProcessor messageProcessor;
    private Main main;

    public ConnectionHandler(Main main, MessageProcessor messageProcessor) {
        this.main = main;
        this.messageProcessor = messageProcessor;
    }

    public void run() {
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.setMessageProcessor(PacketMessage.Head.SHOW,
                                             pm -> Client.setElements(pm.getElements()));
        PacketMessage pm;

        while (true) {
            synchronized (main.getClient()) {
                main.getClient().setSoTimeout(1);
                pm = main.getClient().awaitMessage();
            }
            if (pm != null) {
                messageProcessor.process(pm);
            }
        }
    }
}

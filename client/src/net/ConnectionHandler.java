package net;

public class ConnectionHandler implements Runnable, Cloneable {
    private MessageProcessor messageProcessor;

    public ConnectionHandler(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public void run() {
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.setMessageProcessor(PacketMessage.Head.SHOW,
                                             pm -> Client.setElements(pm.getElements()));
//        messageProcessor.setMessageProcessor(PacketMessage.Head.EMAIL_OK,
//                                             pm -> );
//        messageProcessor.setMessageProcessor(PacketMessage.Head.EMAIL_ERROR,
//                                             pm -> );
        messageProcessor.setMessageProcessor(PacketMessage.Head.LOGIN_OK,
                                             pm -> Client.setToken(pm.getToken()));
        messageProcessor.setMessageProcessor(PacketMessage.Head.LOGIN_ERROR,
                                             pm -> Client.loginError((String) pm.getBody()));
        messageProcessor.setMessageProcessor(PacketMessage.Head.SET_ADDRESS,
                                             pm -> Client.setAddress(pm.getAddress()));
        while (Client.getShouldRun()) {
            PacketMessage pm = Client.getNext();
            messageProcessor.process(pm);
        }
    }
}

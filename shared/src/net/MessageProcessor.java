package net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageProcessor {
    private Map<PacketMessage.Head, Consumer<PacketMessage>> messageMap = new HashMap<>();

    public void setMessageProcessor(PacketMessage.Head type, Consumer<PacketMessage> processor) {
        messageMap.put(type, processor);
    }

    public boolean hasMessageProcessor(PacketMessage.Head type) {
        return messageMap.containsKey(type);
    }

    public void process(PacketMessage packetMessage) {
        if (messageMap.containsKey(packetMessage.getHead())) {
            messageMap.get(packetMessage.getHead()).accept(packetMessage);
        }
    }
}

package se.miun.distsys.listeners;

import se.miun.distsys.messages.*;

public interface ChatMessageListener {
    public void onIncomingChatMessage(ChatMessage chatMessage);
    public void onIncomingJoinMessage(JoinMessage joinMessage);
    public void onIncomingListMessage(ListMessege listMessege);
    public void onIncomingLeaveMessage(LeaveMessage leaveMessage);
    public void onIncomingCheckMessage(CheckMessage checkMessage);
    public void onIncomingResponseMessage(ResponseMessage responseMessage);
    public void onIncomingElecMessage(ElecMessage elecMessage);
    public void onIncomingAliveMessage(AliveMessage aliveMessage);
    public void onIncomingVictoryMessage(VictoryMessage victoryMessage);
    public void onIncomingOrderMessage(OrderMessage orderMessage);
    public void onIncomingRepMessage(RepMessage repMessage);
    public void onIncomingAskRepMessage(AskRepMessage askRepMessage);
    public void onIncomingAskOrderMessage(AskOrderMessage askOrderMessage);
}

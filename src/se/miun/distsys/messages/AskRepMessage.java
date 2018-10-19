package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/19.
 */
public class AskRepMessage extends Message {
    public Integer sequenceNumber;
    public String client;

    public AskRepMessage(Integer sequenceNumber,String client){
        this.sequenceNumber=sequenceNumber;
        this.client=client;
    }
}

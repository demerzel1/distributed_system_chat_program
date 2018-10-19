package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/19.
 */
public class AskOrderMessage extends Message {
    public String name;
    public String chat;
    public String client;

    public AskOrderMessage(String name,String chat,String client){
        this.name=name;
        this.chat=chat;
        this.client=client;
    }
}

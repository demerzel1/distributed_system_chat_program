package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/19.
 */
public class RepMessage extends Message {
    public Integer order=0;
    public String name="";
    public String chat="";
    public String client="";

    public RepMessage(String name,String chat,Integer order,String client){
        this.name=name;
        this.chat=chat;
        this.order=order;
        this.client=client;
    }
}

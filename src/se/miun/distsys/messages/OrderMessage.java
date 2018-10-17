package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/16.
 */
public class OrderMessage extends Message {
    public Integer order=0;
    public String name="";
    public String chat="";

    public OrderMessage(String name,String chat,Integer order){
        this.order=order;
        this.name=name;
        this.chat=chat;
    }
}

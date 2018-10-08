package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/2.
 */
public class ElecMessage extends Message {
    public String name="";
    public int priority=0;

    public ElecMessage(String name,int priority){
        this.name=name;
        this.priority=priority;
    }
}

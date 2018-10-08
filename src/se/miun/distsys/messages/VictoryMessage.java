package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/2.
 */
public class VictoryMessage extends Message {
    public String name="";
    public int priority=0;

    public VictoryMessage(String name,int priority){
        this.name=name;
        this.priority=priority;
    }
}

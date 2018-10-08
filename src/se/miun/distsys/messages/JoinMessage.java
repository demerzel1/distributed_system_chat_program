package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/9/19.
 */
public class JoinMessage extends Message {

    public String str="";
    public int priority=0;

    public JoinMessage(String name,int priority){
        str=name;
        this.priority=priority;
    }
}

package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/10/2.
 */
public class AliveMessage extends Message{
    public String sendName="";
    public String receiveName="";
    public int priority=0;

    public AliveMessage(String sendName,String receiveName,int priority){
        this.sendName=sendName;
        this.receiveName=receiveName;
        this.priority=priority;
    }
}

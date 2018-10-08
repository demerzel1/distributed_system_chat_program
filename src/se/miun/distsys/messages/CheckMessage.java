package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/9/24.
 */
public class CheckMessage extends Message{
    public String name="";

    public CheckMessage(String name){
        this.name=name;
    }
}

package se.miun.distsys.messages;

/**
 * Created by demerzel on 2018/9/24.
 */
public class ResponseMessage extends Message{
    public String name="";
    public ResponseMessage(String name){
        this.name=name;
    }
}

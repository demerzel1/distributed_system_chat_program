package se.miun.distsys.messages;

import java.util.ArrayList;

/**
 * Created by demerzel on 2018/10/23.
 */


public class DeliverMessage extends Message {
    public String name = "";
    public String chat = "";
    public ArrayList<Integer> vectorClock=null;

    public DeliverMessage(String name,String chat,ArrayList vectorClock){
        this.name=name;
        this.chat=chat;
        this.vectorClock=vectorClock;
    }
}

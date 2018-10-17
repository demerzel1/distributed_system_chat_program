package se.miun.distsys.messages;

import java.util.ArrayList;

public class ChatMessage extends Message {

	public String name = "";
	public String chat = "";

	public ArrayList<Integer> vectorClock=null;

	public ChatMessage(String name,String chat) {
		this.chat = chat;
		this.name = name;
	}
    public ChatMessage(String name,String chat,ArrayList vectorClock) {
        this.chat = chat;
        this.name = name;
        this.vectorClock=vectorClock;
    }
}

package se.miun.distsys.messages;

public class ChatMessage extends Message {

	public String chat = "";	
	
	public ChatMessage(String name,String chat) {
		this.chat = name + ": " + chat;
	}
}

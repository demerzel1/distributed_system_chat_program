package se.miun.distsys;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import javafx.util.Pair;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.messages.*;

public class GroupCommuncation {
	
	private int datagramSocketPort = 25541; //You need to change this!
	DatagramSocket datagramSocket = null;	
	boolean runGroupCommuncation = true;	
	MessageSerializer messageSerializer = new MessageSerializer();
	
	//Listeners
	ChatMessageListener chatMessageListener = null;

	public GroupCommuncation() {			
		try {
			runGroupCommuncation = true;				
			datagramSocket = new MulticastSocket(datagramSocketPort);
						
			RecieveThread rt = new RecieveThread();
			rt.start();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		runGroupCommuncation = false;
		System.out.println("shut down");
	}

	class RecieveThread extends Thread{
		
		@Override
		public void run() {
			byte[] buffer = new byte[65536];		
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			
			while(runGroupCommuncation) {
				try {
					datagramSocket.receive(datagramPacket);										
					byte[] packetData = datagramPacket.getData();					
					Message recievedMessage = messageSerializer.deserializeMessage(packetData);					
					handleMessage(recievedMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private void handleMessage (Message message) {
			
			if(message instanceof ChatMessage) {				
				ChatMessage chatMessage = (ChatMessage) message;				
				if(chatMessageListener != null){
					chatMessageListener.onIncomingChatMessage(chatMessage);
				}
			}else if(message instanceof JoinMessage){
				JoinMessage joinMessage=(JoinMessage)message;
				if(chatMessageListener!=null){
					chatMessageListener.onIncomingJoinMessage(joinMessage);
				}
			}else if(message instanceof ListMessege){
				ListMessege listMessege=(ListMessege)message;
				if(chatMessageListener!=null){
					chatMessageListener.onIncomingListMessage(listMessege);
				}
			}else if(message instanceof LeaveMessage){
				LeaveMessage leaveMessage=(LeaveMessage)message;
				if(chatMessageListener!=null){
					chatMessageListener.onIncomingLeaveMessage(leaveMessage);
				}
			}else if(message instanceof  CheckMessage){
				CheckMessage checkMessage=(CheckMessage)message;
				if(chatMessageListener!=null){
					chatMessageListener.onIncomingCheckMessage(checkMessage);
				}
			}else if(message instanceof  ResponseMessage){
				ResponseMessage responseMessage=(ResponseMessage)message;
				if(chatMessageListener!=null){
					chatMessageListener.onIncomingResponseMessage(responseMessage);
				}
			}else if(message instanceof  ElecMessage){
                ElecMessage elecMessage=(ElecMessage)message;
                if(chatMessageListener!=null){
                    chatMessageListener.onIncomingElecMessage(elecMessage);
                }
            }else if(message instanceof  AliveMessage){
                AliveMessage aliveMessage=(AliveMessage)message;
                if(chatMessageListener!=null){
                    chatMessageListener.onIncomingAliveMessage(aliveMessage);
                }
            }else if(message instanceof  VictoryMessage){
                VictoryMessage victoryMessage=(VictoryMessage) message;
                if(chatMessageListener!=null){
                    chatMessageListener.onIncomingVictoryMessage(victoryMessage);
                }
            }
			else {
				System.out.println("Unknown message type");
			}			
		}		
	}	



	public void sendChatMessage(String name,String chat) {
		try {
			ChatMessage chatMessage = new ChatMessage(name,chat);
			byte[] sendData = messageSerializer.serializeMessage(chatMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendJoinMessage(String name,int priority){
		try{
		    System.out.println("sendJoinMessage");
			JoinMessage joinMessage = new JoinMessage(name,priority);
			byte[] sendData = messageSerializer.serializeMessage(joinMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
			datagramSocket.send(sendPacket);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void sendListMessage(ArrayList<Pair<String,Integer>> arrayList){
		try{
			ListMessege listMessege=new ListMessege(arrayList);
			byte[] sendData = messageSerializer.serializeMessage(listMessege);
			DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
			datagramSocket.send(sendPacket);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void sendLeaveMessage(String name){
		try{
			LeaveMessage leaveMessage=new LeaveMessage(name);
			byte[] sendData = messageSerializer.serializeMessage(leaveMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
			datagramSocket.send(sendPacket);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void sendCheckMessage(String name){
		try {
		CheckMessage checkMessage=new CheckMessage(name);
		byte[] sendData= messageSerializer.serializeMessage(checkMessage);
		DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendResponseMessage(String name){
		try{
			ResponseMessage responseMessage=new ResponseMessage(name);
			byte[] sendData = messageSerializer.serializeMessage(responseMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
			datagramSocket.send(sendPacket);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

    public void sendElecMessage(String name,int priority){
        try{
            ElecMessage elecMessage=new ElecMessage(name,priority);
            byte[] sendData = messageSerializer.serializeMessage(elecMessage);
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
            datagramSocket.send(sendPacket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendAliveMessage(String sendName,String receiveName,int priority){
        try{
            AliveMessage aliveMessage=new AliveMessage(sendName,receiveName,priority);
            byte[] sendData = messageSerializer.serializeMessage(aliveMessage);
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
            datagramSocket.send(sendPacket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendVictoryMessage(String name,int priority){
        try{
            VictoryMessage victoryMessage=new VictoryMessage(name,priority);
            byte[] sendData = messageSerializer.serializeMessage(victoryMessage);
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),datagramSocketPort);
            datagramSocket.send(sendPacket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

	public void setChatMessageListener(ChatMessageListener listener) {
		this.chatMessageListener = listener;		
	}
	
}

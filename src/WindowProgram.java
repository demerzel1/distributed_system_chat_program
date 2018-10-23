import java.awt.*;

import javax.swing.*;

import javafx.util.Pair;
import se.miun.distsys.GroupCommuncation;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.messages.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class WindowProgram implements ChatMessageListener, ActionListener {

    static class Node implements Comparable<Node>{
        String name="";
        String chat="";
        Integer order=-1;

        Node(String name,String chat){
            this.name=name;
            this.chat=chat;
        }

        Node(String name,String chat,Integer order){
            this.name=name;
            this.chat=chat;
            this.order=order;
        }

        @Override
        public int compareTo(Node o) {
            if(o.order<order)
                return -1;
            else if(o.order>order)
                return 1;
            else
                return name.compareTo(o.name);
        }
    }

    PriorityBlockingQueue<Node> pbq=new PriorityBlockingQueue<>();

    //remove node from pbq by name and chat
    boolean removeFromPbqByNameAndChat(String name,String chat){
        for(Node node:pbq){
            if(node.name.equals(name)&&node.chat.equals(chat)){
                return pbq.remove(node);
            }
        }
        return false;
    }

    class VectorNode{
        ArrayList<Integer> vectorClock;
        String name;
        String chat;

        public VectorNode(String name,String chat,ArrayList vectorClock){
            this.name=name;
            this.chat=chat;
            this.vectorClock=vectorClock;
        }
    }

    //0:total_sequencer 1:causal vector clock
	int orderFlag=1;

	JFrame frame;
	JTextPane txtpnChat = new JTextPane();
	JTextPane txtpnMessage = new JTextPane();
	String name="";
	JButton exitButton= new JButton("exit");
	JOptionPane optionPane=new JOptionPane();
	public ArrayList<Pair<String,Integer> > activeClient=new ArrayList<>();
	JTextPane txtpnList = new JTextPane();
	JLabel leaderLabel=new JLabel();


	HashMap<String,Boolean> hashMap=new HashMap<>();
	HashMap<String,Integer> missCount=new HashMap<>();

	//priority number of client for bully
	int priority=0;

	boolean flag=false;

	boolean vectorFlag=false;

	Pair<String,Integer> leader=null;
	boolean hasLeader=false;
    boolean isLeader=false;
    boolean hasReceiveAlive=false;

	int sequenceNumber=0;

	int clientMessageCount=0;

	ArrayList<Integer> vectorClock=new ArrayList<>();

	//save the chat message
	ArrayList<Node> holdArrayChatMessage=new ArrayList<>();

	//hold list for vector clock
	ArrayList<VectorNode> holdVectorChatMessage = new ArrayList<>();

	GroupCommuncation gc = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowProgram window = new WindowProgram();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	class ActiveChecker extends Thread{
		@Override
		public void run(){
//			System.out.println("activechecker run");
			while (activeClient!=null){
//				System.out.println(activeClient.size());
				for(Pair<String,Integer> pairClient:activeClient){
				    String client=pairClient.getKey();
					hashMap.put(client,false);
					gc.sendCheckMessage(client);
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	class ResponseChecker extends Thread{
		@Override
		public void run(){
//			System.out.println("ResponseCHecker run");
			while(activeClient!=null){
				for(Pair<String,Integer> pairClient:activeClient){
                    String client=pairClient.getKey();
//					System.out.println(client+" status:"+hashMap.get(client).toString());
					if(hashMap.get(client)==false){
//						System.out.println("missCount: "+client+" "+missCount.get(client).toString());
						missCount.put(client,missCount.get(client)+1);
						if(missCount.get(client)>=5){
							System.out.println(client+" missed!!!");
							gc.sendLeaveMessage(client);
						}
					}
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class LeaderChecker extends Thread{
	    @Override
        public void run(){
	    	int cnt=0;
	        while(true){
	          //  System.out.println(hasLeader+" "+hasReceiveAlive);
	            if(!hasLeader&&!hasReceiveAlive){
					cnt++;
					if(cnt>=3){
						System.out.println(gc.getClass());
						System.out.println("leader:"+name+" "+priority);
					    gc.sendVictoryMessage(name,priority);
					    cnt=0;
                    }
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PbqHolder extends Thread{
	    @Override
        public void run(){
	        while(true){
	            if(!pbq.isEmpty()){
                    Node node= null;
                    try {
                        node = pbq.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    System.out.println("clientMessageCount :"+clientMessageCount+" "+"order: "+node.order);
                    if(clientMessageCount==node.order){
                        txtpnChat.setText(getTime()+" "+node.order+" "+node.name+" : "+node.chat + "\n" + txtpnChat.getText());
                        clientMessageCount++;
                    }else{
                        gc.sendAskOrderMessage(node.name,node.chat,name);
                    }
                }
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class VectorChecker extends Thread{
	    @Override
        public void run(){
	        while(true){
	            if(holdVectorChatMessage.size()!=0){
	                System.out.println(holdVectorChatMessage.size());
	                for(VectorNode vectorNode:holdVectorChatMessage){
	                    int t=getIndexFromActiveClientByName(vectorNode.name);
	                    int sz=vectorNode.vectorClock.size();
	                    boolean flag=true;

	                    for(int i=0;i<sz;++i){
	                        if(i==t){
                                if(vectorClock.get(i)+1!=vectorNode.vectorClock.get(i)){
                                    flag=false;
                                    break;
                                }
                            }else{
	                            if(vectorClock.get(i)<vectorNode.vectorClock.get(i)){
	                                flag=false;
	                                break;
                                }
                            }
                        }
                        if(flag){
                            txtpnChat.setText("vector: "+vectorNode.vectorClock+" " + vectorNode.name+" : "+vectorNode.chat + "\n" + txtpnChat.getText());
                            holdVectorChatMessage.remove(vectorNode);
                            vectorClock.set(t,vectorClock.get(t)+1);
                        }
                    }
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public WindowProgram() {
		initializeFrame();



		gc = new GroupCommuncation();
		gc.setChatMessageListener(this);

		gc.sendJoinMessage(name,priority);
		System.out.println("Group Communcation Started");
		hashMap.put(name,true);
		missCount.put(name,1);
		ActiveChecker activeChecker=new ActiveChecker();
		activeChecker.start();
		ResponseChecker responseChecker=new ResponseChecker();
		responseChecker.start();
        LeaderChecker leaderChecker=new LeaderChecker();
        leaderChecker.start();
        if(orderFlag==0){
            PbqHolder pbqHolder=new PbqHolder();
            pbqHolder.start();
        }
        if(orderFlag==1){
            VectorChecker vectorChecker=new VectorChecker();
            vectorChecker.start();
        }
 	}

	private void initializeFrame() {
		String inputName=optionPane.showInputDialog("please the name：\n");
		name=inputName;

		long l=System.currentTimeMillis();
		priority=(int)(l%1000);

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FlowLayout());
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane);
		scrollPane.setSize(450,100);
		scrollPane.setViewportView(txtpnChat);
		txtpnChat.setEditable(false);
		txtpnChat.setPreferredSize(new Dimension(450,100));
		txtpnChat.setText("---group chat----");

		txtpnMessage.setText("Message");
		txtpnMessage.setPreferredSize(new Dimension(200, 100));

		txtpnList.setText("ActiveList:\n");
		txtpnList.setPreferredSize(new Dimension(200,100));

		frame.getContentPane().add(txtpnMessage);

		frame.getContentPane().add(txtpnList);

		JButton btnSendChatMessage = new JButton("Send Chat Message");
		btnSendChatMessage.addActionListener(this);
		btnSendChatMessage.setActionCommand("send");

		exitButton.addActionListener(this);
		exitButton.setActionCommand("exit");

		leaderLabel.setText("leader: ");

		frame.getContentPane().add(leaderLabel);

		frame.getContentPane().add(btnSendChatMessage);

		frame.getContentPane().add(exitButton);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
				gc.sendLeaveMessage(name);
	        	gc.shutdown();
	        }
	    });
	}

	private String getTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	private int getMaxPriority(){
	    int res=0;
	    for(Pair<String,Integer> pair:activeClient){
	        res=Math.max(res,pair.getValue());
        }
        return res;
    }

    private int getIndexFromActiveClientByName(String clientName){
	    int res=0;
        for(Pair<String,Integer> pair:activeClient){
            if(pair.getKey().equals(clientName)){
                return res;
            }
            res++;
        }
        return -1;
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equalsIgnoreCase("send")) {
		    if(orderFlag==0)
			    gc.sendChatMessage(name,txtpnMessage.getText());
		    if(orderFlag==1){
		        System.out.println("vectorClock size: "+vectorClock.size());
		        int tIndex=getIndexFromActiveClientByName(name);
		        int t=vectorClock.get(tIndex);
                vectorClock.set(tIndex,t+1);
                gc.sendChatMessage(name,txtpnMessage.getText(),vectorClock);
                gc.sendDeliverMessage(name,txtpnMessage.getText(),vectorClock);
            }

		}else if(event.getActionCommand().equalsIgnoreCase("exit")){
			gc.sendLeaveMessage(name);
			gc.shutdown();
			System.exit(0);
		}
	}


	@Override
	public void onIncomingChatMessage(ChatMessage chatMessage) {
        System.out.println("leader: "+leader.getKey()+leader.getValue());
        System.out.println("chat: "+chatMessage.name+chatMessage.chat);
		if(orderFlag==0){
			pbq.add(new Node(chatMessage.name,chatMessage.chat));

//			System.out.println("hasLeader: "+hasLeader);
			if(leader.getKey().equals(name)&&leader.getValue()==priority){
                System.out.println("I'm leader!");
				gc.sendOrderMessage(chatMessage.name,chatMessage.chat,sequenceNumber);
				sequenceNumber++;
				holdArrayChatMessage.add(new Node(chatMessage.name,chatMessage.chat,sequenceNumber));
			}
		}else if(orderFlag==1){
		    int sz=chatMessage.vectorClock.size();
		    int myIndex=getIndexFromActiveClientByName(name);

		    //update vector clock

            System.out.println("chatsize: "+chatMessage.vectorClock.size()+" vectorsize: "+vectorClock.size());
            
            if(chatMessage.name.equals(name)){
                txtpnChat.setText("vector: "+vectorClock+" " + chatMessage.name+" : "+chatMessage.chat + "\n" + txtpnChat.getText());
            }else{
                if(!vectorFlag){
                    int vsz=vectorClock.size();
                    int tind=getIndexFromActiveClientByName(chatMessage.name);
                    for(int i=0;i<vsz;++i){
                        if(i==getIndexFromActiveClientByName(name))
                            continue;
                        if(i==tind){
                            vectorClock.set(i,chatMessage.vectorClock.get(i)-1);
                        }else{
                            vectorClock.set(i,chatMessage.vectorClock.get(i));
                        }
                    }
                    vectorFlag=true;
                }
                holdVectorChatMessage.add(new VectorNode(chatMessage.name,chatMessage.chat,chatMessage.vectorClock));
            }



        }

//		txtpnChat.setText(getTime()+" "+chatMessage.chat + "\n" + txtpnChat.getText());
	}

	@Override
	public void onIncomingJoinMessage(JoinMessage joinMessage) {
		System.out.println("Incoming join message: "+joinMessage.str);
		hashMap.put(joinMessage.str,true);

		txtpnChat.setText(getTime()+" "+"Incoming join message: "+joinMessage.str+"\n"+txtpnChat.getText());
		Pair<String,Integer> pair=new Pair<String,Integer>(joinMessage.str,joinMessage.priority);
		activeClient.add(pair);

		gc.sendListMessage(activeClient,clientMessageCount);

		if(orderFlag==1){
		    vectorClock.add(0);
        }

		//new client join,start election
//        if(priority==getMaxPriority()&&activeClient.size()!=1){
//            //gc.sendVictoryMessage(name,priority);
//            gc.sendElecMessage(name,priority);
//        }else{
//            gc.sendElecMessage(name,priority);
//        }
        hasLeader=false;
		hasReceiveAlive=false;
        gc.sendElecMessage(name,priority);
	}

	@Override
	public void onIncomingListMessage(ListMessege listMessege) {
		if(flag!=true && listMessege.activeList.size()!=1){
			flag=true;
			System.out.println("Incoming list:");
			activeClient=listMessege.activeList;
			clientMessageCount=listMessege.clientMessageNumber;
			System.out.println("clientMessegeCount: "+ clientMessageCount);

			if(orderFlag==1){
                int sz=listMessege.activeList.size();
                int vecSz=vectorClock.size();
                for(int i=0;i<sz-vecSz;++i){
                    vectorClock.add(0);
                }
            }

		}
		System.out.println(activeClient);
		txtpnList.setText("activeClient:\n");
		for(Pair<String,Integer> pairClient:activeClient){
		    String str=pairClient.getKey();
			hashMap.put(str,true);
			txtpnList.setText(txtpnList.getText()+str+" pri: "+pairClient.getValue().toString()+"\n");
			System.out.println(str);
		}
	}

	@Override
	public void onIncomingLeaveMessage(LeaveMessage leaveMessage) {
        String leaveName=leaveMessage.str;

        //get the index of leave client by name
        int indexOfClient=-1;
        int sz=activeClient.size();
        for(int i=0;i<sz;++i){
            if(activeClient.get(i).getKey().equals(leaveName)){
                indexOfClient=i;
                break;
            }
        }
        if(indexOfClient==-1){
            return;
        }

	    txtpnList.setText("activeClient:\n");
		txtpnChat.setText(getTime()+" "+leaveName+" leave the group chat.\n"+txtpnChat.getText());

        System.out.println(indexOfClient);
        activeClient.remove(indexOfClient);

        //client leave,start election
//        if(priority==getMaxPriority()){
//            //gc.sendVictoryMessage(name,priority);
//            gc.sendElecMessage(name,priority);
//        }else{
//            gc.sendElecMessage(name,priority);
//        }
        hasLeader=false;
        hasReceiveAlive=false;
        gc.sendElecMessage(name,priority);

		System.out.println("activeClient:");
		for (Pair<String,Integer> pairClient:activeClient) {
		    String str=pairClient.getKey();
			txtpnList.setText(txtpnList.getText()+str+" pri: "+pairClient.getValue().toString()+"\n");
			System.out.println(str);
		}

		if(orderFlag==1){
            vectorClock.remove(indexOfClient);
        }
	}

	@Override
	public void onIncomingCheckMessage(CheckMessage checkMessage) {
//		System.out.println("Incoming CheckMessage");
		if(checkMessage.name.equals(name)){
//			System.out.println("receive checkMessage for self");
			gc.sendResponseMessage(name);
		}
	}

	@Override
	public void onIncomingResponseMessage(ResponseMessage responseMessage) {
		if(hashMap.get(responseMessage.name)==false){
			hashMap.put(responseMessage.name,true);
			missCount.put(responseMessage.name,0);
		}
	}

    @Override
    public void onIncomingElecMessage(ElecMessage elecMessage) {
	    System.out.println("receive elecMessage "+elecMessage.name+" "+((Integer)elecMessage.priority).toString());
	   // hasLeader=false;
	    if(elecMessage.priority>=priority){
	        return;
        }
        hasLeader=false;
        gc.sendAliveMessage(name,elecMessage.name,priority);
	    gc.sendElecMessage(name,priority);
        hasReceiveAlive=false;
    }

    @Override
    public void onIncomingAliveMessage(AliveMessage aliveMessage) {
	    System.out.println("receive AliveMessage: sendName:"+aliveMessage.sendName+" receiveName: "+aliveMessage.receiveName+" priority: "+((Integer)aliveMessage.priority).toString());
        if(!aliveMessage.receiveName.equals(name)){
            return;
        }
        hasReceiveAlive=true;
    }

    @Override
    public void onIncomingVictoryMessage(VictoryMessage victoryMessage) {
	    System.out.println("receive VictoryMessage: name: "+victoryMessage.name+" priority: "+((Integer)victoryMessage.priority).toString());
        if(leader!=null&&leader.getKey().equals(victoryMessage.name)){
            hasLeader=true;
            return;
        }
        boolean flag=false;
        if(leader==null){
            flag=true;
        }
	    hasLeader=true;
        leader=new Pair<>(victoryMessage.name,victoryMessage.priority);
        System.out.println("leader: "+leader.toString());
        leaderLabel.setText("leader: "+leader.getKey()+" pri: "+leader.getValue().toString()+" ");
        if(victoryMessage.name.equals(name)){
            isLeader=true;
        }
        
        //for order
        if(orderFlag==0){
            if(!flag||leader.getKey().equals(name))
                clientMessageCount=0;
            if(leader.getKey().equals(name)&&leader.getValue()==priority){
                sequenceNumber=0;
            }
        }
    }

	@Override
	public void onIncomingOrderMessage(OrderMessage orderMessage) {
        if(orderFlag==0){
            if(removeFromPbqByNameAndChat(orderMessage.name,orderMessage.chat)){
                System.out.println("orderMessage: "+orderMessage.name+orderMessage.chat+orderMessage.order);
                pbq.add(new Node(orderMessage.name,orderMessage.chat,orderMessage.order));
                try {
                    Node node=pbq.take();
                    System.out.println("clientMessageCount :"+clientMessageCount+" "+"order: "+node.order);
                    if(clientMessageCount==node.order){
                        txtpnChat.setText(getTime()+" "+node.order+" "+node.name+" : "+node.chat + "\n" + txtpnChat.getText());
                        clientMessageCount++;
                    }else{
                        pbq.add(node);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else{
                gc.sendAskRepMessage(orderMessage.order,name);
            }
        }
	}

    @Override
    public void onIncomingRepMessage(RepMessage repMessage) {
	    if(name.equals(repMessage.client)){
	        pbq.add(new Node(repMessage.name,repMessage.chat,repMessage.order));
            try {
                Node node=pbq.take();
                System.out.println("clientMessageCount :"+clientMessageCount+" "+"order: "+node.order);
                if(clientMessageCount==node.order){
                    txtpnChat.setText(getTime()+" "+node.order+" "+node.name+" : "+node.chat + "\n" + txtpnChat.getText());
                    clientMessageCount++;
                }else{
                    pbq.add(node);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onIncomingAskRepMessage(AskRepMessage askRepMessage) {
        if(leader.getKey().equals(name)){
            int sz=holdArrayChatMessage.size();
            for(int i=sz-1;i>=0;--i){
                Node node=holdArrayChatMessage.get(i);
                if(node.order==askRepMessage.sequenceNumber){
                    gc.sendRepMessage(node.name,node.chat,node.order,askRepMessage.client);
                    break;
                }
            }
        }
    }

    @Override
    public void onIncomingAskOrderMessage(AskOrderMessage askOrderMessage) {
        if(leader.getKey().equals(name)){
            int sz=holdArrayChatMessage.size();
            for(int i=sz-1;i>=0;--i){
                Node node=holdArrayChatMessage.get(i);
                if(node.name.equals(askOrderMessage.name)&&node.chat.equals(askOrderMessage.chat)){
                    gc.sendRepMessage(node.name,node.chat,node.order,askOrderMessage.client);
                    break;
                }
            }
        }
    }

    @Override
    public void onIncomingDeliverMessage(DeliverMessage deliverMessage) {
	    if(deliverMessage.name.equals(name)){
	        return;
        }
        int ind=-1;
        int sz=holdVectorChatMessage.size();
        for(int i=0;i<sz;++i){
            VectorNode vectorNode=holdVectorChatMessage.get(i);
            if(deliverMessage.name.equals(vectorNode.name)&&deliverMessage.vectorClock.equals(vectorNode.vectorClock)&&deliverMessage.chat.equals(vectorNode.chat)){
                ind=i;
                break;
            }
        }
        if(ind==-1){
            holdVectorChatMessage.add(new VectorNode(deliverMessage.name,deliverMessage.chat,deliverMessage.vectorClock));
            ind=holdVectorChatMessage.size()-1;
        }
        int vecsz=vectorClock.size();
        int j=getIndexFromActiveClientByName(deliverMessage.name);

        VectorNode vectorNode=holdVectorChatMessage.get(ind);

        System.out.println(vectorNode.vectorClock);
        System.out.println(vectorClock);

        System.out.println("ind: "+ind);

        boolean flag=true;
        for(int i=0;i<vecsz;++i){
            if(i==j){
                if(vectorClock.get(i)+1!=vectorNode.vectorClock.get(i)){
                    flag=false;
                    break;
                }
            }else{
                if(vectorNode.vectorClock.get(i)>vectorClock.get(i)){
                    flag=false;
                    break;
                }
            }
        }

        if(flag){
            txtpnChat.setText("vector: "+vectorNode.vectorClock+" " + vectorNode.name+" : "+vectorNode.chat + "\n" + txtpnChat.getText());
            holdVectorChatMessage.remove(ind);
            vectorClock.set(j,vectorClock.get(j)+1);
        }
    }
}

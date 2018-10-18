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
        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         *
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         *
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         *
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
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

    //0:total_sequencer 1:causal vector clock
	int orderFlag=0;

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

	Pair<String,Integer> leader=null;
	boolean hasLeader=false;
    boolean isLeader=false;
    boolean hasReceiveAlive=false;

	int sequenceNumber=0;

	int clientMessageCount=0;

	ArrayList<Integer> vectorClock=new ArrayList<>();

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
	            if(!hasLeader&&!hasReceiveAlive){
					cnt++;
					if(cnt>=3){
						System.out.println(gc.getClass());
						System.out.println("leader:"+name+" "+priority);
					    gc.sendVictoryMessage(name,priority);
					    cnt=0;
                    }
                    try {
                        sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class PbqHolder extends Thread{
	    @Override
        public void run(){
	        while(true){
	            if(!pbq.isEmpty()){

                }
                try {
                    sleep(500);
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
			}
		}else if(orderFlag==1){
		    int sz=chatMessage.vectorClock.size();

		    int myIndex=getIndexFromActiveClientByName(name);


		    //update vector clock

            System.out.println("chatsize: "+chatMessage.vectorClock.size()+" vectorsize: "+vectorClock.size());

		    for(int i=0;i<sz;++i){
		        if(i == myIndex){
		            if(!chatMessage.name.equals(name)){
                        int t=vectorClock.get(i);
                        vectorClock.set(i,t+1);
                    }
		            continue;
                }
		        int t=Math.max(vectorClock.get(i),chatMessage.vectorClock.get(i));
		        vectorClock.set(i,t);
            }

            txtpnChat.setText("vector: "+vectorClock+" " + chatMessage.name+" : "+chatMessage.chat + "\n" + txtpnChat.getText());

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
        if(priority==getMaxPriority()&&activeClient.size()!=1){
            gc.sendVictoryMessage(name,priority);
        }else{
            gc.sendElecMessage(name,priority);
        }
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
        if(priority==getMaxPriority()){
            gc.sendVictoryMessage(name,priority);
        }else{
            gc.sendElecMessage(name,priority);
        }


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
	    hasLeader=false;
	    if(elecMessage.priority>=priority){
	        return;
        }
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

            }
        }
	}
}

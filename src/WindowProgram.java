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

public class WindowProgram implements ChatMessageListener, ActionListener {

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
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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


	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equalsIgnoreCase("send")) {
			gc.sendChatMessage(name,txtpnMessage.getText());
		}else if(event.getActionCommand().equalsIgnoreCase("exit")){
			gc.sendLeaveMessage(name);
			gc.shutdown();
			System.exit(0);
		}
	}


	@Override
	public void onIncomingChatMessage(ChatMessage chatMessage) {
		txtpnChat.setText(getTime()+" "+chatMessage.chat + "\n" + txtpnChat.getText());
	}

	@Override
	public void onIncomingJoinMessage(JoinMessage joinMessage) {
		System.out.println("Incoming join message: "+joinMessage.str);
		hashMap.put(joinMessage.str,true);

		txtpnChat.setText(getTime()+" "+"Incoming join message: "+joinMessage.str+"\n"+txtpnChat.getText());
		Pair<String,Integer> pair=new Pair<String,Integer>(joinMessage.str,joinMessage.priority);
		activeClient.add(pair);
		gc.sendListMessage(activeClient);

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
			txtpnList.setText(txtpnList.getText()+str+"\n");
			System.out.println(str);
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
	    isLeader=false;
	    if(elecMessage.priority>=priority){
	        return;
        }
        gc.sendAliveMessage(name,elecMessage.name,priority);
	    gc.sendElecMessage(name,priority);
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
        if(hasLeader==true&&leader.getKey().equals(victoryMessage.name)){
            return;
        }
	    hasLeader=true;
        leader=new Pair<>(victoryMessage.name,victoryMessage.priority);
        System.out.println("leader: "+leader.toString());
        leaderLabel.setText("leader: "+leader.getKey()+" pri: "+leader.getValue().toString()+" ");
        if(victoryMessage.name.equals(name)){
            isLeader=true;
        }
    }
}
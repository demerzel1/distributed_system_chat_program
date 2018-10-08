package se.miun.distsys.messages;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by demerzel on 2018/9/19.
 */
public class ListMessege extends Message {
    public ArrayList<Pair<String,Integer> > activeList=new ArrayList<>();
    public ListMessege(ArrayList<Pair<String,Integer> > arrayList){
        activeList=arrayList;
    }
}

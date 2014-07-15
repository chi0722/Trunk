package control.impl;

public class CityWinner {

    /* record the winner of each city card,
     * in order to show in the end of hand
     */
    
    int uid, cid;
    int numOfGangsters;
    Snapshot[] snapshots;
    
    CityWinner(int cid, Snapshot[] snapshots) {
    
        this.cid = cid;
        this.snapshots = snapshots;
        nextHand();
    }
    
    public void nextHand() {

        uid = -1;
        numOfGangsters = 0;    
    }
    
    public boolean hasWinner() {
        
        return (uid != -1);
    }
    
    public String getName() {
        
        if (hasWinner())
            return snapshots[uid].getBossName();
        else
            return "None";
    }
    
    public void setCityWinner(int id, int numOfGangsterPut) {
        
        int numOfGangsters = snapshots[id].getGangstersIncity(cid) +
                             numOfGangsterPut;
        if (numOfGangsters <= this.numOfGangsters)
            throw new RuntimeException();
        
        this.numOfGangsters = numOfGangsters;
        this.uid = id;
    }
    
    public boolean isCityWinner(int id, int numOfGangsterPut) {
        
        if (id == uid || id == -1) return true;
        
        int numOfGangsters = snapshots[id].getGangstersIncity(cid) +
                             numOfGangsterPut;
        return (numOfGangsters > this.numOfGangsters);
    }

}

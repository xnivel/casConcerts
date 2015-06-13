package casConcerts;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BoxOffice {
    private Random random;
    private TicketsSession session;
    private int limit=10;
    public AtomicInteger lwtCount;

    public BoxOffice() {
        this.lwtCount = new AtomicInteger(0);

        session = new TicketsSession("127.0.0.1");
        random = new Random();
    }

    public void init(String concert, int type, int maxTickets) {
        session.insertMaxTickets(concert, type, maxTickets);
        for(int i=0;i<maxTickets;i++){
            session.insertNewTickets(concert, type, i);
        }
        for(int i=0;i<maxTickets;i+=limit){
            session.insertInterval(concert,type,i);
        }
    }


    public int buyTicket(String name, String concert, int type) {
        int id = findFreeId(concert, type);
        if (id < 0) {
            return -1;
        }

        session.addToCandidates(name, concert, type, id);
        Set<String> candidates = session.getCandidates(concert, type, id);
        if (candidates.size() == 1 && candidates.contains(name)) {
            session.setOwner(name, concert, type, id);
            return id;
        }

        this.lwtCount.getAndIncrement();

        boolean ok = session.setOwnerTransaction(name, concert, type, id);
        if (ok) {
            return id;
        } else {
            return -1;
        }
    }

    public int findFreeId(String concert, int type) {
        int maxTickets = session.getMaxTickets(concert, type);

        boolean foundFree = false;
        int id = -1;

        for (int i = 0; i < 3; i++) {
            id = random.nextInt(maxTickets);
            if (session.isFree(concert, type, id)) {
                foundFree = true;
                break;
            }
        }
        ArrayList<Integer> intervalsList = session.getIntervals(concert,type);
        int tmpid;
        int index = random.nextInt(intervalsList.size());
        while (!foundFree) {
            if(intervalsList.size()==0){
                return -1;
            }
            tmpid=session.getFreeTicket(concert,type,intervalsList.get(index),limit);
            if(tmpid==-1){
                session.removeInterval(concert,type,intervalsList.get(index));
                intervalsList.remove(index);
                if(intervalsList.size()==0){
                    return -1;
                }
                index = random.nextInt(intervalsList.size());
            }else{
                return tmpid;
            }
        }

        return id;
    }

    public void nuke() {
        session.deleteAllTickets();
        session.deleteAllTicketsInfo();
    }
}

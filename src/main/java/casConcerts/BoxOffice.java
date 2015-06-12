package casConcerts;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class BoxOffice {
    private Random random;
    private TicketsSession session;

    public BoxOffice() {
        session = new TicketsSession("127.0.0.1");
        random = new Random();
    }

    public void init(String concert, int type, int maxTickets) {
        session.insertMaxTickets(concert, type, maxTickets);
        for(int i=0;i<maxTickets;i++){
            session.insertFreeTicket(concert,type,i);
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
            session.deleteFreeTicket(concert, type, id);
            return id;
        }

        boolean ok = session.setOwnerTransaction(name, concert, type, id);
        if (ok) {
            session.deleteFreeTicket(concert, type, id);
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

        while (!foundFree) {
            ArrayList<Integer> freeIds = session.getFreeTickets(concert, type);
            if (freeIds.size() == 0) {
                return -1;
            }
            id = freeIds.get(random.nextInt(freeIds.size()));
        }

        return id;
    }

    public void nuke() {
        session.deleteAllFreeTickets();
        session.deleteAllTickets();
        session.deleteAllTicketsInfo();
    }
}

package casConcerts;

import java.util.ArrayList;
import java.util.Random;

public class BoxOffice {
    private ITicketsSession session;
    private Random random;

    public BoxOffice() {
        session = new TicketsSession("127.0.0.1");
        random = new Random();
    }

    public void init(String concert, int type, int maxTickets) {
        session.init(concert, type, maxTickets);
    }

    public int buyTicket(String name, String concert, int type) {
        int id = findFreeId(concert, type);
        if (id < 0) {
            return -1;
        }

        session.addToCandidates(name, concert, type, id);
        ArrayList<String> candidates = session.getCandidates(name, concert, type, id);
        if (candidates.size() == 1 && candidates.get(0).equals(name)) {
            session.setOwner(name, concert, type, id);
            return id;
        }

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
        int id;

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

    public void returnTicket(String name,String concert, int type,int count,long timestamp) {
        long[] selectResults = session.select(concert, type);
        if(timestamp==0)
            timestamp=selectResults[1]+100000;
        if (selectResults[0] != selectResults[2]) {
            session.increment(name, concert, type, count, (int) selectResults[2], timestamp, selectResults[0] < selectResults[2] * 0.25);
        }
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

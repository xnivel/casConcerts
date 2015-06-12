package casConcerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketsSession {
    private static final Logger logger = LoggerFactory
            .getLogger(TicketsSession.class);

    public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";
    public static TicketsSession instance = null;

    private Session session;

    public static TicketsSession getSession() {
        if (instance != null)
            return instance;

        synchronized (TicketsSession.class) {
            if (instance == null)
                instance = new TicketsSession(null);
        }

        return instance;
    }

    public TicketsSession(String contactPoint) {
        if (contactPoint == null || contactPoint.isEmpty())
            contactPoint = DEFAULT_CONTACT_POINT;

        Cluster cluster = Cluster.builder()
                .addContactPoint(contactPoint).build();
        session = cluster.connect("casConcerts");

        prepareStatements();
    }

    private static PreparedStatement INSERTFREETICKET;
    private static PreparedStatement DELETEFREETICKET;
    private static PreparedStatement ADD_TO_CANDIDATES;
    private static PreparedStatement GET_CANDIDATES;
    private static PreparedStatement GET_MAX_TICKETS;
    private static PreparedStatement GET_OWNER;
    private static PreparedStatement SET_OWNER;
    private static PreparedStatement SET_OWNER_TRANSACTION;
    private static PreparedStatement GET_FREE_TICKETS;

    private static PreparedStatement DELETE_ALL_FREETICKETS;
    private static PreparedStatement DELETE_ALL_TICKETS;
    private static PreparedStatement DELETE_ALL_TICKETSINFO;

    private static final String TICKET_FORMAT = "- %-15s %-2s %-10s %-10s\n";
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        INSERTFREETICKET = session.prepare("INSERT INTO freetickets (concert,type,id) VALUES (?,?,?)").setConsistencyLevel(ConsistencyLevel.ONE);
        DELETEFREETICKET = session.prepare("DELETE from freetickets WHERE concert = ? and type = ? and id = ? ;").setConsistencyLevel(ConsistencyLevel.ONE);

        DELETE_ALL_FREETICKETS = session.prepare("TRUNCATE freetickets;");
        DELETE_ALL_TICKETS = session.prepare("TRUNCATE tickets;");
        DELETE_ALL_TICKETSINFO = session.prepare("TRUNCATE ticketsinfo;");

        ADD_TO_CANDIDATES = session.prepare("UPDATE tickets SET candidates = candidates + ? WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        GET_CANDIDATES = session.prepare("SELECT candidates FROM tickets WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        GET_MAX_TICKETS = session.prepare("SELECT maxTickets FROM ticketsInfo WHERE concert = ? and type = ?").setConsistencyLevel(ConsistencyLevel.ONE);
        GET_OWNER = session.prepare("SELECT owner FROM tickets WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        SET_OWNER = session.prepare("UPDATE tickets SET owner = ? WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        SET_OWNER_TRANSACTION = session.prepare("UPDATE tickets SET owner = ? WHERE concert = ? and type = ? and id = ? IF owner = NULL").setConsistencyLevel(ConsistencyLevel.QUORUM);
        GET_FREE_TICKETS = session.prepare("SELECT id FROM freetickets WHERE concert = ? and type = ?").setConsistencyLevel(ConsistencyLevel.ONE);

        logger.info("Statements prepared");
    }
    public void insertFreeTicket(String name,int type,int id){
        BoundStatement bs;
        bs = new BoundStatement(INSERTFREETICKET.setConsistencyLevel(ConsistencyLevel.QUORUM));
        bs.bind(name,type,id);
        session.execute(bs);
    }
    public void deleteFreeTicket(String concert, int type, int id){
        BoundStatement bs;
        bs = new BoundStatement(DELETEFREETICKET.setConsistencyLevel(ConsistencyLevel.QUORUM));
        bs.bind(concert, type, id);
        session.execute(bs);
    }

    public void deleteAllFreeTickets() {
        BoundStatement bs = new BoundStatement(DELETE_ALL_FREETICKETS);
        session.execute(bs);
    }
    public void deleteAllTickets() {
        BoundStatement bs = new BoundStatement(DELETE_ALL_TICKETS);
        session.execute(bs);
    }
    public void deleteAllTicketsInfo() {
        BoundStatement bs = new BoundStatement(DELETE_ALL_TICKETSINFO);
        session.execute(bs);
    }

    public void addToCandidates(String name, String concert, int type, int id) {
        BoundStatement bs = new BoundStatement(ADD_TO_CANDIDATES);
        Set<String> set = new HashSet<>();
        set.add(name);
        bs.bind(set, concert, type, id);
        session.execute(bs);
    }

    public Set<String> getCandidates(String concert, int type, int id) {
        BoundStatement bs = new BoundStatement(GET_CANDIDATES);
        bs.bind(concert, type, id);
        ResultSet rs = session.execute(bs);
        Row row = rs.one();
        return row.getSet("candidates", String.class);
    }

    public int getMaxTickets(String concert, int type) {
        BoundStatement bs = new BoundStatement(GET_MAX_TICKETS);
        bs.bind(concert, type);
        ResultSet rs = session.execute(bs);
        Row row = rs.one();
        return row.getInt("maxTickets");
    }

    public boolean isFree(String concert, int type, int id) {
        BoundStatement bs = new BoundStatement(GET_OWNER);
        bs.bind(concert, type, id);
        ResultSet rs = session.execute(bs);
        Row row = rs.one();
        String owner = row.getString("owner");
        return owner == null;
    }

    public void setOwner(String name, String concert, int type, int id) {
        BoundStatement bs = new BoundStatement(SET_OWNER);
        bs.bind(name, concert, type, id);
        session.execute(bs);
    }

    public boolean setOwnerTransaction(String name, String concert, int type, int id) {
        BoundStatement bs = new BoundStatement(SET_OWNER_TRANSACTION);
        bs.bind(name, concert, type, id);
        ResultSet rs = session.execute(bs);
        Row row = rs.one();
        return row.getBool("[applied]");
    }

    public ArrayList<Integer> getFreeTickets(String concert, int type) {
        BoundStatement bs = new BoundStatement(GET_FREE_TICKETS);
        bs.bind(concert, type);
        ResultSet rs = session.execute(bs);

        ArrayList<Integer> ids = new ArrayList<>();
        for (Row row : rs) {
            int id = row.getInt("id");
            ids.add(id);
        }
        return ids;
    }

    protected void finalize() {
        try {
            if (session != null) {
                session.getCluster().close();
            }
        } catch (Exception e) {
            logger.error("Could not close existing cluster", e);
        }
    }
}

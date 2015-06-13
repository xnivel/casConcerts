package casConcerts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static PreparedStatement ADD_TO_TICKETS;

    private static PreparedStatement ADD_TO_CANDIDATES;
    private static PreparedStatement GET_CANDIDATES;

    private static PreparedStatement GET_MAX_TICKETS;
    private static PreparedStatement SET_MAX_TICKETS;

    private static PreparedStatement GET_OWNER;
    private static PreparedStatement SET_OWNER;
    private static PreparedStatement SET_OWNER_TRANSACTION;

    private static PreparedStatement DELETE_ALL_TICKETS;
    private static PreparedStatement DELETE_ALL_TICKETSINFO;
    private static PreparedStatement DELETE_ALL_INTERVALSTICKETS;

    private static PreparedStatement GET_MORE_TICKETS;

    private static PreparedStatement SET_INTERVAL;
    private static PreparedStatement GET_INTERVAL;
    private static PreparedStatement REMOVE_INTERVAL;


    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        ADD_TO_TICKETS = session.prepare("INSERT INTO tickets (concert , type , id) VALUES (?,?,?)").setConsistencyLevel(ConsistencyLevel.QUORUM);

        DELETE_ALL_TICKETS = session.prepare("TRUNCATE tickets;");
        DELETE_ALL_TICKETSINFO = session.prepare("TRUNCATE ticketsinfo;");
        DELETE_ALL_INTERVALSTICKETS = session.prepare("TRUNCATE intervaltickets;");


        ADD_TO_CANDIDATES = session.prepare("UPDATE tickets SET candidates = candidates + ? WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        GET_CANDIDATES = session.prepare("SELECT candidates FROM tickets WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        GET_MAX_TICKETS = session.prepare("SELECT maxTickets FROM ticketsInfo WHERE concert = ? and type = ?").setConsistencyLevel(ConsistencyLevel.ONE);
        SET_MAX_TICKETS = session.prepare("UPDATE ticketsInfo SET maxTickets=? WHERE concert = ? and type = ?").setConsistencyLevel(ConsistencyLevel.ONE);

        GET_OWNER = session.prepare("SELECT owner FROM tickets WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        SET_OWNER = session.prepare("UPDATE tickets SET owner = ? WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
        SET_OWNER_TRANSACTION = session.prepare("UPDATE tickets SET owner = ? WHERE concert = ? and type = ? and id = ? IF owner = NULL").setConsistencyLevel(ConsistencyLevel.QUORUM);

        GET_MORE_TICKETS = session.prepare("SELECT id,owner FROM tickets WHERE concert = ? and type = ? and id>=? limit ?").setConsistencyLevel(ConsistencyLevel.ONE);

        GET_INTERVAL = session.prepare("SELECT id  FROM intervaltickets WHERE concert = ? and type = ?").setConsistencyLevel(ConsistencyLevel.ONE);
        SET_INTERVAL = session.prepare("INSERT INTO intervaltickets (concert , type , id) VALUES (?,?,?)").setConsistencyLevel(ConsistencyLevel.ONE);
        REMOVE_INTERVAL = session.prepare("DELETE from intervaltickets WHERE concert = ? and type = ? and id = ?").setConsistencyLevel(ConsistencyLevel.ONE);


        logger.info("Statements prepared");
    }
    public void insertInterval(String name,int type,int id){
        BoundStatement bs;
        bs = new BoundStatement(SET_INTERVAL);
        bs.bind(name,type,id);
        session.execute(bs);
    }
    public void removeInterval(String name,int type,int id){
        BoundStatement bs;
        bs = new BoundStatement(REMOVE_INTERVAL);
        bs.bind(name,type,id);
        session.execute(bs);
    }
    public ArrayList<Integer> getIntervals(String name,int type){
        BoundStatement bs;
        bs = new BoundStatement(GET_INTERVAL);
        bs.bind(name,type);
        ResultSet rs = session.execute(bs);
        ArrayList<Integer> ids = new ArrayList<>();
        for (Row row : rs) {
            int id = row.getInt("id");
            ids.add(id);
        }
        return ids;
    }
    public void insertNewTickets(String name,int type,int id){
        BoundStatement bs;
        bs = new BoundStatement(ADD_TO_TICKETS);
        bs.bind(name,type,id);
        session.execute(bs);
    }
    public void insertMaxTickets(String name,int type,int max){
        BoundStatement bs;
        bs = new BoundStatement(SET_MAX_TICKETS);
        bs.bind(max,name,type);
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
    public void deleteAllIntervalstickets() {
        BoundStatement bs = new BoundStatement(DELETE_ALL_INTERVALSTICKETS);
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
        Set<String> can = row.getSet("candidates", String.class);
        return owner == null && can.size()==0;
    }
    public int getFreeTicket(String concert, int type, int id,int limit) {
        BoundStatement bs = new BoundStatement(GET_MORE_TICKETS);
        bs.bind(concert, type, id,limit);
        ResultSet rs = session.execute(bs);
        for(Row row : rs){
            String owner = row.getString("owner");
            if(owner == null){
                return row.getInt("id");
            }
        };
        return -1;
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

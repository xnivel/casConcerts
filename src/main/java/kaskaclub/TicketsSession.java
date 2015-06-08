package kaskaclub;

import java.text.SimpleDateFormat;

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
        session = cluster.connect("kaskaclub");

        prepareStatements();
    }

    private static PreparedStatement INIT;
    private static PreparedStatement SELECT;
    private static PreparedStatement SELECT_ALL;
    private static PreparedStatement INCREMENTWITHTS;
    private static PreparedStatement DECREMENTWITHTS;
    private static PreparedStatement DELETE_ALL;

    private static final String TICKET_FORMAT = "- %-15s %-2s %-10s %-10s\n";
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        INIT = session.prepare("UPDATE tickets SET count = count + ? where concert = ? and type = ? and maxTickets = ?");
        SELECT = session.prepare("SELECT count,blobAsBigInt(timestampAsBlob(dateof(now()))),maxTickets FROM tickets WHERE concert = ? and type = ?;").setConsistencyLevel(ConsistencyLevel.ONE);
        SELECT_ALL = session.prepare("SELECT * FROM tickets;");
        INCREMENTWITHTS = session.prepare(
                "UPDATE tickets USING TIMESTAMP ? SET count = count + 1 WHERE concert = ? and type = ?;");
        DECREMENTWITHTS = session.prepare(
                "UPDATE tickets USING TIMESTAMP ? SET count = count - 1 WHERE concert = ? and type = ?;");
        DELETE_ALL = session.prepare("TRUNCATE tickets;");
        logger.info("Statements prepared");
    }

    public String selectAll() {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL);
        ResultSet rs = session.execute(bs);
        for (Row row : rs) {
            String concert = row.getString("concert");
            int type = row.getInt("type");
            int maxTickets = row.getInt("maxTickets");
            long count = row.getLong("count");

            builder.append(String.format(TICKET_FORMAT, concert, type, maxTickets, count));
        }

        return builder.toString();
    }

    public void init(String concert, int type, int maxTickets) {
        BoundStatement bs = new BoundStatement(INIT);
        bs.bind((long)maxTickets, concert, type, maxTickets);
        session.execute(bs);

        logger.info("Max tickets for " + concert + " type " + type + " set to " + maxTickets);
    }

    public long[] select(String concert, int type) {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT);
        bs.bind(concert, type);
        ResultSet rs = session.execute(bs);
        Row row=rs.one();
        System.out.println(row.getColumnDefinitions().toString());
        long count =row.getLong("count");
        long timestamp = row.getLong(1);
        long maxtickets = (long)row.getInt("maxTickets");

        logger.info("Ticket count for " + concert + " type " + type + " count " + count + " selected");
        long[] result = {count,timestamp,maxtickets};
        return result;
    }

    public void increment(String concert, int type,long timestamp,boolean accurate) {
        BoundStatement bs;
        if(accurate)
             bs = new BoundStatement(INCREMENTWITHTS.setConsistencyLevel(ConsistencyLevel.QUORUM));
        else
            bs = new BoundStatement(INCREMENTWITHTS.setConsistencyLevel(ConsistencyLevel.ANY));
        bs.bind(timestamp,concert, type);
        session.execute(bs);

        logger.info("Ticket count for " + concert + " type " + type + " incremented");
    }

    public void decrement(String concert, int type,long timestamp,boolean accurate) {
        BoundStatement bs;
        if (accurate)
            bs= new BoundStatement(DECREMENTWITHTS.setConsistencyLevel(ConsistencyLevel.QUORUM));
        else
            bs= new BoundStatement(DECREMENTWITHTS.setConsistencyLevel(ConsistencyLevel.ANY));
        bs.bind(timestamp,concert, type);
        session.execute(bs);

        logger.info("Ticket count for " + concert + " type " + type + " decremented");
    }

    public void deleteAll() {
        BoundStatement bs = new BoundStatement(DELETE_ALL);
        session.execute(bs);

        logger.info("All tickets deleted");
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

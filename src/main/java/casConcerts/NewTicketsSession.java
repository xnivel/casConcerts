package casConcerts;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public class NewTicketsSession implements ITicketsSession {
    private static final Logger logger = LoggerFactory
            .getLogger(NewTicketsSession.class);

    public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";
    public static NewTicketsSession instance = null;

    private Session session;

    public static NewTicketsSession getSession() {
        if (instance != null)
            return instance;

        synchronized (NewTicketsSession.class) {
            if (instance == null)
                instance = new NewTicketsSession(null);
        }

        return instance;
    }

    public NewTicketsSession(String contactPoint) {
        if (contactPoint == null || contactPoint.isEmpty())
            contactPoint = DEFAULT_CONTACT_POINT;

        Cluster cluster = Cluster.builder()
                .addContactPoint(contactPoint).build();
        session = cluster.connect("casConcerts");

        prepareStatements();
    }

    private static PreparedStatement INIT;
    private static PreparedStatement SELECT;
    private static PreparedStatement SELECT_ALL;
    private static PreparedStatement INCREMENT;
    private static PreparedStatement DECREMENT;
    private static PreparedStatement DELETE_ALL;

    private static PreparedStatement INSERTBUYER;
    private static PreparedStatement DELETEBUYER;

    private static final String TICKET_FORMAT = "- %-15s %-2s %-10s %-10s\n";
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        INIT = session.prepare("UPDATE tickets SET count = ? where concert = ? and type = ? and maxTickets = ?");
        SELECT = session.prepare("SELECT count,blobAsBigInt(timestampAsBlob(dateof(now()))),maxTickets FROM tickets WHERE concert = ? and type = ?;").setConsistencyLevel(ConsistencyLevel.ONE);
        SELECT_ALL = session.prepare("SELECT * FROM tickets;");
        INCREMENT = session.prepare(
                "UPDATE tickets SET count = ? WHERE concert = ? and type = ? and maxTickets = ? if count = ?;");
        DECREMENT = session.prepare(
                "UPDATE tickets SET count = ? WHERE concert = ? and type = ? and maxTickets = ? if count = ?;");
        DELETE_ALL = session.prepare("TRUNCATE tickets;");


        INSERTBUYER = session.prepare("UPDATE ticketsboughtby USING TIMESTAMP ? SET  count = ?,timestamp=? WHERE name = ? and concert = ? and type = ?;");
        DELETEBUYER = session.prepare("DELETE from ticketsboughtby USING TIMESTAMP ? WHERE concert = ? and type = ? and name = ? ;");

        logger.info("Statements prepared");
    }

    @Override
    public String selectAll() {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL);
        ResultSet rs = session.execute(bs);
        for (Row row : rs) {
            String concert = row.getString("concert");
            int type = row.getInt("type");
            int maxTickets = row.getInt("maxTickets");
            int count = row.getInt("count");

            builder.append(String.format(TICKET_FORMAT, concert, type, maxTickets, count));
        }

        return builder.toString();
    }

    @Override
    public void init(String concert, int type, int maxTickets) {
        BoundStatement bs = new BoundStatement(INIT.setConsistencyLevel(ConsistencyLevel.ALL));
        bs.bind(maxTickets, concert, type, maxTickets);
        session.execute(bs);

        logger.info("Max tickets for " + concert + " type " + type + " set to " + maxTickets);
    }

    @Override
    public long[] select(String concert, int type) {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT);
        bs.bind(concert, type);
        ResultSet rs = session.execute(bs);
        Row row=rs.one();
        long count =row.getInt("count");
        long timestamp = row.getLong(1);
        long maxtickets = (long)row.getInt("maxTickets");

        logger.info("Ticket count for " + concert + " type " + type + " count " + count + " selected");
        long[] result = {count,timestamp,maxtickets};
        return result;
    }

    @Override
    public Boolean increment(String name, String concert, int type, int count, int maxTickets, long timestamp, boolean accurate) {
        BoundStatement bs;
        boolean correct_execute = false;
        while(!correct_execute) {
            try {
                long[] selectResult = select(concert, type);
                int oldCount = (int)selectResult[0];

                if (oldCount >= maxTickets) {
                    logger.info("Ticket count for " + concert + " type " + type + " is at max");
                    return false;
                }

                int newCount = oldCount + count;

                bs= new BoundStatement(INCREMENT.setConsistencyLevel(ConsistencyLevel.QUORUM));
                bs.bind(newCount,concert, type, maxTickets, oldCount);

                ResultSet rs = session.execute(bs);
                Row row = rs.one();
                if (row.getBool("[applied]")) {
                    correct_execute = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        bs= new BoundStatement(DELETEBUYER.setConsistencyLevel(ConsistencyLevel.ONE));
        bs.bind(timestamp+1,concert, type, name);
        session.execute(bs);

        logger.info("Ticket count for " + concert + " type " + type + " incremented");

        return true;
    }

    @Override
    public Boolean decrement(String name, String concert, int type, int count, int maxTickets, long timestamp, boolean accurate) {
        BoundStatement bs;
        boolean correct_execute=false;
        while(!correct_execute) {
            try {
                long[] selectResult = select(concert, type);
                int oldCount = (int)selectResult[0];

                if (oldCount == 0) {
                    logger.info("Ticket count for " + concert + " type " + type + " is 0");
                    return false;
                }

                int newCount = oldCount - count;

                bs= new BoundStatement(DECREMENT.setConsistencyLevel(ConsistencyLevel.QUORUM));
                bs.bind(newCount,concert, type, maxTickets, oldCount);

                ResultSet rs = session.execute(bs);
                Row row = rs.one();
                if (row.getBool("[applied]")) {
                    correct_execute = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        bs = new BoundStatement(INSERTBUYER.setConsistencyLevel(ConsistencyLevel.QUORUM));
        bs.bind(timestamp,count,timestamp,name,concert, type);
        session.execute(bs);

        logger.info("Ticket count for " + concert + " type " + type + " decremented");
        return true;
    }

    @Override
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

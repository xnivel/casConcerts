package kaskaclub;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

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

    private static PreparedStatement SELECT_ALL;
    private static PreparedStatement INCREMENT;
    private static PreparedStatement DECREMENT;
    private static PreparedStatement DELETE_ALL;

    private static final String TICKET_FORMAT = "- %-15s %-2s %-10s\n";
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        SELECT_ALL = session.prepare("SELECT * FROM tickets;");
        INCREMENT = session.prepare(
                "UPDATE tickets SET count = count + 1 WHERE concert = ? and type = ?;");
        DECREMENT = session.prepare(
                "UPDATE tickets SET count = count - 1 WHERE concert = ? and type = ?;");
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
            long count = row.getLong("count");

            builder.append(String.format(TICKET_FORMAT, concert, type, count));
        }

        return builder.toString();
    }

    public void increment(String concert, int type) {
        BoundStatement bs = new BoundStatement(INCREMENT);
        bs.bind(concert, type);
        session.execute(bs);

        logger.info("Ticket count for " + concert + " type " + type + " incremented");
    }

    public void decrement(String concert, int type) {
        BoundStatement bs = new BoundStatement(DECREMENT);
        bs.bind(concert, type);
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

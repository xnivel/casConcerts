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

public class UsersSession {

    private static final Logger logger = LoggerFactory
            .getLogger(UsersSession.class);

    public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";
    public static UsersSession instance = null;

    private Session session;


    public static UsersSession getSession() {
        if (instance != null)
            return instance;

        synchronized (UsersSession.class) {
            if (instance == null)
                instance = new UsersSession(null);
        }

        return instance;
    }

    public UsersSession(String contactPoint) {
        if (contactPoint == null || contactPoint.isEmpty())
            contactPoint = DEFAULT_CONTACT_POINT;

        Cluster cluster = Cluster.builder()
                .addContactPoint(contactPoint).build();
        session = cluster.connect("Test");

        prepareStatements();
    }

    private static PreparedStatement SELECT_ALL_FROM_USERS;
    private static PreparedStatement INSERT_INTO_USERS;
    private static PreparedStatement DELETE_ALL_FROM_USERS;

    private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() {
        SELECT_ALL_FROM_USERS = session.prepare(
                "SELECT * FROM users;");
        INSERT_INTO_USERS = session.prepare(
                "INSERT INTO users (companyName, name, phone, street) VALUES (?, ?, ?, ?);");
        DELETE_ALL_FROM_USERS = session.prepare("TRUNCATE users;");
        logger.info("Statements prepared");
    }

    public String selectAll() {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_USERS);
        ResultSet rs = session.execute(bs);
        for (Row row : rs) {
            String rcompanyName = row.getString("companyName");
            String rname = row.getString("name");
            int rphone = row.getInt("phone");
            String rstreet = row.getString("street");

            builder.append(String.format(USER_FORMAT, rcompanyName, rname, rphone,
                    rstreet));
        }

        return builder.toString();
    }

    public void upsertUser(String companyName, String name, int phone, String street) {
        BoundStatement bs = new BoundStatement(INSERT_INTO_USERS);
        bs.bind(companyName, name, phone, street);
        session.execute(bs);

        logger.info("User " + name + " upserted");
    }

    public void deleteAll() {
        BoundStatement bs = new BoundStatement(DELETE_ALL_FROM_USERS);
        session.execute(bs);

        logger.info("All users deleted");
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

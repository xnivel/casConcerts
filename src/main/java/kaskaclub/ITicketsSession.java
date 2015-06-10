package kaskaclub;

/**
 * Created by xnivel on 10.06.15.
 */
public interface ITicketsSession {
    String selectAll();

    void init(String concert, int type, int maxTickets);

    long[] select(String concert, int type);

    void increment(String name, String concert, int type, int count, int maxTickets, long timestamp, boolean accurate);

    void decrement(String name, String concert, int type, int count, int maxTickets, long timestamp, boolean accurate);

    void deleteAll();
}

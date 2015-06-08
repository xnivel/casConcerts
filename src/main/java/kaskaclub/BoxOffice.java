package kaskaclub;

public class BoxOffice {
    private TicketsSession session = new TicketsSession("127.0.0.1");

    public void init(String concert, int type, int maxTickets) {
        session.init(concert, type, maxTickets);
    }

    public void buyTicket(String concert, int type) {
        long[] selectResults = session.select(concert, type);
        session.decrement(concert, type, (int)selectResults[2], selectResults[0] < selectResults[2] * 0.75);
    }

    public void returnTicket(String concert, int type) {
        long[] selectResults = session.select(concert, type);
        session.increment(concert, type, (int)selectResults[2], selectResults[0] < selectResults[2] * 0.75);
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

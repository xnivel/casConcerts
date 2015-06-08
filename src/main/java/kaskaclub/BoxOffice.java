package kaskaclub;

public class BoxOffice {
    private TicketsSession session = new TicketsSession("127.0.0.1");

    public void init(String concert, int type, int maxTickets) {
        session.init(concert, type, maxTickets);
    }

    public long buyTicket(String concert, int type) {
            long[] selectResults=session.select(concert, type);
            session.decrement(concert, type,selectResults[1],selectResults[0]>selectResults[2]*0.75);
            return selectResults[1];
    }

    public void returnTicket(String concert, int type,long timestamp) {
        session.increment(concert, type,timestamp!=0?(session.select(concert,type))[1]:timestamp,(session.select(concert,type))[0]>15);
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

package kaskaclub;

public class BoxOffice {
    private TicketsSession session = new TicketsSession("127.0.0.1");

    public long buyTicket(String concert, int type) {
            long[] selectResults=session.select(concert, type);
            session.increment(concert, type,selectResults[1],selectResults[0]>15);
            return selectResults[1];
    }

    public void returnTicket(String concert, int type,long timestamp) {
        session.decrement(concert, type,timestamp!=0?(session.select(concert,type))[1]:timestamp,(session.select(concert,type))[0]>15);
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

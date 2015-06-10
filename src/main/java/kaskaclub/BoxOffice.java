package kaskaclub;

public class BoxOffice {
    private ITicketsSession session;

    public BoxOffice(Boolean useNew) {
        if (useNew) {
            session = new NewTicketsSession("127.0.0.1");
        } else {
            session = new TicketsSession("127.0.0.1");
        }
    }

    public void init(String concert, int type, int maxTickets) {
        session.init(concert, type, maxTickets);
    }

    public long buyTicket(String name,String concert, int type,int count) {
        long[] selectResults = session.select(concert, type);
        if (selectResults[0] != 0) {
            session.decrement(name,concert, type,count, (int)selectResults[2],selectResults[1], selectResults[0] < selectResults[2] * 0.25);
            return selectResults[1];
        } else {
            return -1;
        }
    }

    public void returnTicket(String name,String concert, int type,int count,long timestamp) {
        long[] selectResults = session.select(concert, type);
        if(timestamp==0)
            timestamp=selectResults[1]+100000;
        if (selectResults[0] != selectResults[2]) {
            session.increment(name, concert, type, count, (int) selectResults[2], timestamp, selectResults[0] < selectResults[2] * 0.25);
        }
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

package kaskaclub;

public class BoxOffice {
    private TicketsSession session = new TicketsSession("127.0.0.1");

    public boolean buyTicket(String concert, int type) {
        if ((session.select(concert,type))>15) {
            session.increment(concert, type);
            return true;
        } else {
            return false;
        }
    }

    public void returnTicket(String concert, int type) {
        session.decrement(concert, type);
    }

    public void nuke() {
        session.deleteAll();
    }

    public String report() {
        return session.selectAll();
    }
}

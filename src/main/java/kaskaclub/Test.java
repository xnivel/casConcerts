package kaskaclub;

public class Test extends Thread {
    int iteration;
    String concertName;
    int type;
    int sleeptime;

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public void setConcertName(String concertName) {
        this.concertName = concertName;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSleeptime(int sleeptime) {
        this.sleeptime = sleeptime;
    }
    public void setAll(int iteration,String concertName,int type,int sleeptime){
        this.iteration = iteration;
        this.concertName = concertName;
        this.type = type;
        this.sleeptime = sleeptime;
    }

    public void run(){
        BoxOffice boxOffice = new BoxOffice();
        long timestamp=0;

        for(int i=0;i<iteration;i++){
            timestamp=boxOffice.buyTicket(concertName, type);
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boxOffice.returnTicket(concertName, type,timestamp);


        }
    }
}

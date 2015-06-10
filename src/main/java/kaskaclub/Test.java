package kaskaclub;

public class Test extends Thread {
    int iteration;
    String concertName;
    String userName;
    int count;
    int type;
    int sleeptime;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCount(int count) {
        this.count = count;
    }

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
    public void setAll(int iteration,String concertName,int type,int sleeptime,String userName,int count){
        this.iteration = iteration;
        this.concertName = concertName;
        this.type = type;
        this.sleeptime = sleeptime;
        this.userName = userName;
        this.count = count;
    }

    public void run(){
        BoxOffice boxOffice = new BoxOffice(true);
        long timestamp=0;

        for(int i=0;i<iteration;i++){
            timestamp=boxOffice.buyTicket(userName,concertName, type,count);
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boxOffice.returnTicket(userName,concertName, type,count,timestamp);


        }
    }
}

package casConcerts;

import java.util.HashMap;

public class TestOnlyBuy extends Thread {
    int iteration;
    String concertName;
    String userName;
    int count;
    int type;
    int sleeptime;
    BoxOffice boxOffice;

    public int missCount=0;
    HashMap<Integer,Integer> MapOfBoughtTickets= new HashMap<Integer,Integer>();

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
    public void setAll(int iteration,String concertName,int type,int sleeptime,String userName,int count,BoxOffice boxOffice){
        this.iteration = iteration;
        this.concertName = concertName;
        this.type = type;
        this.sleeptime = sleeptime;
        this.userName = userName;
        this.count = count;
        this.boxOffice = boxOffice;
    }

    public void run(){
        for(int i=0;i<iteration;i++){
            int id = -1;
            while (id < 0) {
                id = boxOffice.buyTicket(userName, concertName, type);
                if (id == -2) {
                    System.out.println("No intervals left :(");
                    return;
                }
                missCount++;
                System.out.print(".");
            }
            missCount--;
            Integer tmp=MapOfBoughtTickets.get(id);
            if(tmp!=null){
                MapOfBoughtTickets.put(id, tmp + 1);
            }else{
                MapOfBoughtTickets.put(id,1);
            }

            System.out.println();
            System.out.println(i + ": Ticket bought successfully, id = " + id);
        }
    }
}

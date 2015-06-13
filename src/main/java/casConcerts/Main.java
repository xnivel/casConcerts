/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package casConcerts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void showhelp() {
        System.out.println("i-init");
        System.out.println("b-buy");
        System.out.println("c-clear");
        System.out.println("e-exit");
        System.out.println("h-help");
        System.out.println("t-test");
        System.out.println("m-multi-test");
        System.out.println("M-Buy only test");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BoxOffice boxOffice = new BoxOffice();
        boolean exit = false;
        long timestamp= 0;

        String comand;
        Scanner cin = new Scanner(System.in);
        showhelp();

        while (!exit) {
            System.out.println("Put command[]:");

            comand = cin.nextLine();

            if (comand.contains("i")) {
                boxOffice.init("Slayer", 1, 1000);
            }
            if (comand.contains("M")) {
                //String[] names = {"Alice", "Bob", "Clive", "Daria", "Eve"};
                ArrayList<String> names = new ArrayList<>();
                for (int i = 0; i < 500; i++) {
                    names.add("u" + i);
                }
                boxOffice.init("Slayer", 1, 50000);
                ArrayList<TestOnlyBuy> tests = new ArrayList<>();
                for (String name : names) {
                    TestOnlyBuy t = new TestOnlyBuy();
                    t.setAll(100, "Slayer", 1, 500, name, 1,boxOffice);
                    tests.add(t);
                }
                long start_time = System.nanoTime();

                for (TestOnlyBuy t: tests) {
                    t.start();
                }
                for (TestOnlyBuy t: tests) {
                    t.join();
                }
                System.out.println("ilosc lwt: "+boxOffice.lwtCount.get());
                long end_time = System.nanoTime();
                int missCount=0;
                HashMap<Integer,Integer> MapOfBoughtTickets= new HashMap<Integer,Integer>();

                for (TestOnlyBuy t: tests) {
                    missCount+=t.missCount;
                    for(Map.Entry<Integer,Integer> e: t.MapOfBoughtTickets.entrySet()){
                        Integer tmp=MapOfBoughtTickets.get(e.getKey());
                        if(tmp!=null){
                            MapOfBoughtTickets.put(e.getKey(), tmp + e.getValue());
                        }else{
                            MapOfBoughtTickets.put(e.getKey(), 1);
                        }
                    }
                }
                System.out.println("miss count - " + missCount);
                int boughtbymorethenone=0;
                for(Map.Entry<Integer,Integer> e: MapOfBoughtTickets.entrySet()){
                    if(e.getValue()>1){
                        System.out.println("k: "+e.getKey()+" v: "+e.getValue());
                        boughtbymorethenone++;
                    }
                }
                System.out.println("tickets sell to more then one - " + boughtbymorethenone);
                System.out.println("tickets sold - "+MapOfBoughtTickets.size());

                double difference = (end_time - start_time)/1e9;
                System.out.println("time - "+difference);
            }
            if (comand.contains("h")) {
                showhelp();
            }
            if (comand.contains("t")) {
                Test t = new Test();
                t.setAll(100, "Slayer", 1, 1000,"Bob",1,boxOffice);
                t.run();
            }
            if (comand.contains("m")) {
                String[] names = {"Alice", "Bob", "Clive", "Daria", "Eve"};
                String[] concerts = {"Slayer", "La Roux", "Moby", "New Order", "Kraftwerk"};
                ArrayList<Test> tests = new ArrayList<>();
                for (String concert: concerts) {
                    boxOffice.init(concert, 1, 50);
                    boxOffice.init(concert, 2, 100);
                    boxOffice.init(concert, 3, 500);
                    for (String name : names) {
                        Test t = new Test();
                        t.setAll(100, concert, 1, 1000, name, 1,boxOffice);
                        tests.add(t);
                    }
                }
                for (Test t: tests) {
                    t.start();
                }
                for (Test t: tests) {
                    t.join();
                }
            }
            if (comand.contains("b")) {
                int result=boxOffice.buyTicket("Bob", "Slayer", 1);
                while (result == -1) {
                    result=boxOffice.buyTicket("Bob", "Slayer", 1);
                };
                System.out.println("buied ticket "+result);
            }

            if (comand.contains("c"))
                boxOffice.nuke();
            if (comand.contains("e"))
                exit = true;

        }

        System.exit(0);
    }
}

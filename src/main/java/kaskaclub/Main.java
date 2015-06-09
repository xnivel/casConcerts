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
package kaskaclub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void showhelp() {
        System.out.println("i-init");
        System.out.println("b-buy");
        System.out.println("p-print");
        System.out.println("s-sell");
        System.out.println("e-exit");
        System.out.println("h-help");
        System.out.println("t-test");
    }

    public static void main(String[] args) throws IOException {
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
                boxOffice.init("Slayer", 1, 10);
            }
            if (comand.contains("h")) {
                showhelp();
            }
            if (comand.contains("t")) {
                Test t = new Test();
                t.setAll(100, "Slayer", 1, 1000,"Bob",1);
                t.run();
            }
            if (comand.contains("b"))
                timestamp=boxOffice.buyTicket("Bob","Slayer", 1,1);
            if (comand.contains("p"))
                System.out.print(boxOffice.report());
            if (comand.contains("s"))
                boxOffice.returnTicket("Bob","Slayer", 1,1,timestamp);
            if (comand.contains("c"))
                boxOffice.nuke();
            if (comand.contains("e"))
                exit = true;

        }

        System.exit(0);
    }
}

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

public class Main {

    public static void main(String[] args) throws IOException {

        UsersSession session = new UsersSession("127.0.0.1");

        session.upsertUser("PP", "Adam", 609, "A St");
        session.upsertUser("PP", "Ola", 509, null);
        session.upsertUser("UAM", "Ewa", 720, "B St");
        session.upsertUser("PP", "Kasia", 713, "C St");

        String output = session.selectAll();
        System.out.println("Users: \n" + output);

        session.deleteAll();

        System.exit(0);

    }
}

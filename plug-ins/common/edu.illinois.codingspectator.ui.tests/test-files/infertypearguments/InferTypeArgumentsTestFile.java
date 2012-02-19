/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

import java.util.ArrayList;
import java.util.List;

public class InferTypeArgumentsTestFile {

    List m() {
        List list= new ArrayList();
        list.add(new InferTypeArgumentsTestFile());
        list.add(list.get(0));
        return list;
    }

}

package com.droidmare.accounts.utils;

import com.droidmare.accounts.models.Multimedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Utils for managing sort operations over file lists:
//@author Eduardo on 24/06/2019.
class SortUtils {

    //Method that returns a sorted multimedia array list:
    static ArrayList<Multimedia> sortMultimediaList (ArrayList<Multimedia> unsortedList) {

        ArrayList<Multimedia> sortedList = new ArrayList<>();

        for (Multimedia unsortedMultimedia: unsortedList)

            for (int i = 0; i <= sortedList.size(); i++) {

                if (i != sortedList.size()) {

                    Multimedia sortedMultimedia = sortedList.get(i);

                    int unsortedType = unsortedMultimedia.getType();
                    int sortedType = sortedMultimedia.getType();

                    String unsortedName = unsortedMultimedia.getName();
                    String sortedName = sortedMultimedia.getName();

                    if ((unsortedType < sortedType) || (unsortedType == sortedType && alphabeticallyPrevious(unsortedName, sortedName))) {
                        sortedList.add(i, unsortedMultimedia);
                        break;
                    }
                }

                else {
                    sortedList.add(unsortedMultimedia);
                    break;
                }
            }

        return sortedList;
    }

    //Method that returns whether or not a string is alphabetically previous to another one:
    private static boolean alphabeticallyPrevious(String unsorted, String sorted) {

        ArrayList<String> auxList = new ArrayList<>();

        auxList.add(unsorted);
        auxList.add(sorted);

        Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
            public int compare(String str1, String str2) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                if (res == 0) {
                    res = str1.compareTo(str2);
                }
                return res;
            }
        };

        Collections.sort(auxList, ALPHABETICAL_ORDER);

        return auxList.get(0).equals(unsorted);
    }

}

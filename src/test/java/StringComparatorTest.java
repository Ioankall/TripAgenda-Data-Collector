package tests;

import utilities.StringComparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Gika on 29/1/2017.
 */

class StringComparatorTest {

    @org.junit.jupiter.api.Test
    void compareStrings() {
        String str1 = null;
        String str2 = null;
        String str3 = "";
        String str4 = "";
        String str5 = "abcd";
        String str6 = "qwrt";
        String str7 = "test";
        String str8 = "tech";

        assert (StringComparator.compareStrings(str1,str2) == 0);
        assert (StringComparator.compareStrings(str3,str4) == 0);
        assert (StringComparator.compareStrings(str1,str4) == 0);
        assert (StringComparator.compareStrings(str5,str6) == 0);
        assert (StringComparator.compareStrings(str1,str6) == 0);
        assert (StringComparator.compareStrings(str3,str6) == 0);
        assert (StringComparator.compareStrings(str7,str7) == 1);
        assert (StringComparator.compareStrings(str7,str8) > 0);
        assert (StringComparator.compareStrings(str7,str8) < 1);
    }

}
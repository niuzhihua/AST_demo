package com.nzh.ast.demo;

import com.nzh.ast.demo.java_8_stream.StreamDemo;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testStreamApi() throws Exception {

        StreamDemo demo = new StreamDemo();
        List<String> result = demo.demo1_filter();
        assertTrue("rng".equals(result.get(0)));    // OK

        List<Integer> result2 = demo.demo2_sorted();
        int lastValue = result2.get(result2.size() - 1);
        assertTrue(10 == lastValue);    // OK

        demo.demo3_map_reduce();                        // OK

        boolean[] result4 = demo.demo4_match();         // OK
        assertTrue(result4[0] == false);
        assertTrue(result4[1] == true);
        assertTrue(result4[2] == false);

        long count = demo.demo5_count();                // OK
        assertTrue(count == 3);

//        demo.demo6_reduce();
        int[] result5 = demo.demo6_1_reduce(false);  // OK
        int[] result5_ = demo.demo6_1_reduce(true);
        assertTrue(result5[0] == result5[1]);
        assertTrue(result5_[0] == result5_[1]);

        demo.demo7_parallel(true);      // OK
        demo.demo7_parallel(false);

        List<String> result8 = demo.demo8_limit();             // OK
        assertTrue(result8.size() == 2);
        assertTrue(result8.get(0).equals("rng"));

        demo.demo9_Collectors_join();       //OK
        Map<String, List<String>> map = demo.demo9_1_Collectors_to_map(); //OK
        assertTrue(map.size() == 2);

        Set<String> result9 = demo.demo9_2_Collectors_to_set();
        assertTrue(result9.size() == 6);

        List<String> result10 = demo.demo10_flatMap();          // OK
        assertTrue("sublist-2".equals(result10.get(0)));

        demo.demo11_flatMap();

        List<String> result11 = demo.demo11_distinct();         // OK
        long tesCount = result11.stream().filter(str -> str.equals("tes")).count();
        assertTrue(tesCount == 1);


    }


}
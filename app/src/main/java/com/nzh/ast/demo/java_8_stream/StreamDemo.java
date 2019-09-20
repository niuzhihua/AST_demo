package com.nzh.ast.demo.java_8_stream;

import android.util.Printer;

import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 31414 on 2019/9/6.
 * <p>
 * 对实现了 java.util.Collection 接口的类做流的操作
 */
public class StreamDemo {

    private List<String> getList() {
        List<String> list = new ArrayList<>();
        list.add("rng");
        list.add("edg");
        list.add("ig");
        list.add("tes");
        list.add("fpx");
        list.add("jd");
        return list;
    }

    // 过滤 操作 :
    public List<String> demo1_filter() {

        List<String> list = getList();
        // 过滤出带g 的字符串
        // 排除 带 i 和 “ed”的字符串
        List<String> result = list.stream()
                .filter((s) -> s.contains("g"))
                .filter((s) -> !s.contains("i"))
                .filter((s) -> !s.contains("ed"))
                .collect(Collectors.toList());
        Consumer<String> consumer = System.out::println;
        result.forEach(consumer);
        return result;
    }

    // 排序操作
    public List<Integer> demo2_sorted() {
        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(4);
        list.add(6);
        list.add(1);
        list.add(10);
        list.add(2);

        Comparator<Integer> comparator = (i, j) -> i > j ? 0 : -1;

        List<Integer> result = list.stream().sorted(comparator).collect(Collectors.toList());

        result.forEach(System.out::print);
        return result;

    }

    /**
     * map ； 将 List 中的每一个元素打印，并以 "_" 分割
     */
    public String demo3_map_reduce() {

        List<String> list = getList();

        // 1 ：定义处理函数
        Function<String, String> fun = StreamDemo::addLine;

        // 使用map 操作符 对每一个 元素处理。
//        list.stream().map(fun).forEach(System.out::print);

        String result = list.stream().map(fun).reduce("\r\n#", (str1, str2) -> str1 + str2);
        System.out.println(result);
        // 或
        String result2 = list.stream().reduce("\r\n#", (str1, str2) -> str1 + "_" + str2);
        System.out.println(result2);

        return result;
    }

    public static String addLine(String str) {
        return str + "_";
    }

    /**
     * match 操作符：检查集合符合某一个条件
     */
    public boolean[] demo4_match() {
        List<String> list = getList();
        Predicate<String> condition = s -> s.contains("g");
        Predicate<String> condition2 = s -> s.contains("x");

        boolean allMatch = list.stream().allMatch(condition);   // false
        boolean anyMatch = list.stream().anyMatch(condition);   // true
        boolean noneMatch = list.stream().noneMatch(condition2);  //false

        boolean[] result = {allMatch, anyMatch, noneMatch};
        System.out.println("allMatch:" + allMatch + " anyMatch:" + anyMatch + " noneMatch:" + noneMatch);
        return result;

    }

    /**
     * count 操作： 统计流中符合条件的元素总数
     */
    public long demo5_count() {
        List<String> list = getList();

        long count = list.stream().filter(s -> s.contains("g")).count();
        System.out.println("count:" + count);

        return count;
    }

    /**
     * reduce ：应用场景: 基本数据类型的运算。
     * 1:将集合中的每一个数据 给指定的函数处理。当前数据处理的输出 作为下一个处理的输入。
     * 2: 这就要求处理函数的 参数类型，返回值类型 和 集合中的类型一致，
     */
    public void demo6_reduce() {
        List<String> list = getList();

        BinaryOperator<String> operator = (s1, s2) -> s1 + "_" + s2;
        // 方式1 : 返回的数据和 Stream 流中元素类型一致
        Optional<String> optional = list.stream().reduce(operator);
        // 方式2：处理流中的元素时，先将identity处理。 返回的数据和 Stream 流中元素类型一致
        String result = list.stream().reduce("#", operator);

        optional.ifPresent(System.out::print);
        System.out.println(result);

    }

    /**
     * reduce 带三个参数 的例子
     *
     * @param isParallel 是否是并发流的情况
     * @return
     */
    public int[] demo6_1_reduce(boolean isParallel) {
        // 方式3
        // 第一个参数返回实例u，传递你要返回的U类型对象的初始化实例u
        // 第二个参数累加器accumulator，可以使用二元ℷ表达式（即二元lambda表达式），声明你在u上累加你的数据来源t的逻辑
        // 例如(u,t)->u.sum(t),此时lambda表达式的行参列表是返回实例u和遍历的集合元素t，函数体是在u上累加t
        // 第三个参数 是二元 表达式 , 只并发流下才有用。表示 处理并发计算的结果

        // 函数体返回的类型则要和第一个参数的类型保持一致

        List<Integer> list = Arrays.asList(2, 4, 6);
        int init = 3;
        Integer result = -1;    // 累加器计算的结果
        int temp;   // 存放自己推算的结果
        if (isParallel) {
            result = list.stream().parallel().reduce(init, (a, b) -> (a + b), (a, b) -> {
                System.out.println("-------");
                return a + b;
            });
            temp = list.size() * init + 2 + 4 + 6;
        } else {
            result = list.stream().reduce(init, (a, b) -> {
                // 观测累加器的规律，本次计算的输出 是下次计算的输入。
                System.out.println(" a:" + a + " b:" + b);
                return a + b;
            }, (a, b) -> {
                return 0;  // 非并发流下随便写，不报错就行。
            });
            temp = init + 2 + 4 + 6;
        }

        return new int[]{result, temp};
    }


    /**
     * 并行操作流
     */
    public void demo7_parallel(boolean isParallel) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(UUID.randomUUID().toString());
        }
        // 纳秒
        long t0 = System.nanoTime();
        long count = 0;
        if (isParallel) {
            count = list.parallelStream().sorted().count();
        } else {
            count = list.stream().sorted().count();
        }

        long t1 = System.nanoTime();

        // 纳秒转微秒
        long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);

        if (isParallel) {
            System.out.println(String.format("并行流排序耗时: %d ms", millis));
        } else {
            System.out.println(String.format("非并行流排序耗时: %d ms", millis));
        }

    }

    /**
     * limit : 限制流中元素的最大数量
     */
    public List<String> demo8_limit() {
        List<String> list = getList();
        // 过滤前3条数据

        return list.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * collect ：数据收集器
     */
    public void demo9_Collectors_join() {
        List<String> list = new ArrayList<>();
        list.add("rng");
        list.add("fpx");
        list.add("fpx");
        list.add("ig");
        list.add("ig");


        String result1 = list.stream().collect(Collectors.joining());
        String result2 = list.stream().collect(Collectors.joining("-"));
        String result3 = list.stream().collect(Collectors.joining("-", "<我是开头>", "<我是结尾>"));

        System.out.println("\r\n集合内字符链接：");
        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);

    }

    public Set<String> demo9_2_Collectors_to_set() {
        List<String> list = getList();
        // 添加一个重复的元素
        list.add("tes");
        list.add("jd");
        list.add("rng");

        // String : 输入类型
        // Set<String> : 返回类型
        Collector<String, ?, Set<String>> c = Collectors.toSet();
        Set<String> sets = list.stream().collect(c);
        System.out.println(sets);

        return sets;
    }

    /**
     * 生成map : 需要确保 key 不会重复，否则转换的过程将会直接抛出异常。
     */
    public Map<String, List<String>> demo9_1_Collectors_to_map() {
        List<Person> list = new ArrayList<>();

        List<String> list1 = new ArrayList<>();
        list1.add("list1");
        list1.add("list111");
        list.add(new Person("abc", list1));

        List<String> list2 = new ArrayList<>();
        list2.add("list2");
        list2.add("list222");
        list.add(new Person("def", list2));

        // 输入类型Person ,返回类型 String
        Function<Person, String> key = person -> person.getName();  // 生成key 数据
        Function<Person, List<String>> value = person -> person.getCars();  // 生成value 数据

        Map<String, List<String>> map = list.stream().collect(Collectors.toMap(key, value));

        System.out.println(map.toString());
        return map;
    }

    /**
     * https://www.mkyong.com/java8/java-8-flatmap-example/
     * flatMap 使用场景1：对于数据流中的每一个元素 是可以转化为流的，相当于数据至少是一个二维 的形式。
     */
    public List<String> demo10_flatMap() {
        String[][] data = new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}};

        List<String> result = Arrays.stream(data)
                .flatMap(array -> Arrays.stream(array))
                .filter(string -> "a".equals(string) || "c".equals(string)).collect(Collectors.toList());

        System.out.println(result);

        List<Person> list = new ArrayList<>();

        List<String> subList = new ArrayList<>();
        subList.add("sublist-1");

        List<String> subList2 = new ArrayList<>();
        subList2.add("sublist-2");

        list.add(new Person("abc", subList));
        list.add(new Person("def", subList2));


        List<String> subListResult = list.stream()
                .flatMap(p -> p.cars.stream())
                .filter(car -> car.contains("2"))
                .collect(Collectors.toList());
        return subListResult;
    }

    /**
     * flatmap 使用场景2：将二维数据 选出来，放入一维数据中。 （需要有一个二维的数据结构）
     * <p>
     * 和场景1 一样，只不过配合 map 操作。.
     */
    public void demo11_flatMap() {
        Person obj1 = new Person();
        obj1.setName("mkyong");
        obj1.addBook("Java 8 in Action");
        obj1.addBook("Spring Boot in Action");
        obj1.addBook("Effective Java (2nd Edition)");

        Person obj2 = new Person();
        obj2.setName("zilap");
        obj2.addBook("Learning Python, 5th Edition");   // 第二层数据Set
        obj2.addBook("Effective Java (2nd Edition)");

        List<Person> list = new ArrayList<>();  // 第一层数据 List
        list.add(obj1);
        list.add(obj2);

        List<String> result =
                list.stream()
                        .map(person -> person.getBook())      //Stream<Set<String>>
                        .flatMap(set -> set.stream())   //Stream<String>  最终返回的数据类型是第二层的数据类型
                        .collect(Collectors.toList());

        System.out.println("------------------------");
        result.forEach(x -> System.out.println(x));

    }

    /**
     * distinct ： 去除 集合中的 重复元素
     */
    public List<String> demo11_distinct() {
        List<String> list = getList();
        list.add("tes");
        list.add("tes");
        list.add("tes");

        List<String> result1 = list.stream().distinct().collect(Collectors.toList());

        return result1;
    }


    class Person {
        private String name;
        List<String> cars;
        Set<String> book;

        public Person(String name, List<String> cars) {
            this.name = name;
            this.cars = cars;
        }

        public Person(String name) {
            this.name = name;
        }

        public Person() {
        }

        public Set<String> getBook() {
            return book;
        }

        public List<String> getCars() {
            return cars;
        }

        public void addBook(String book) {
            if (this.book == null) {
                this.book = new HashSet<>();
            }
            this.book.add(book);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }


        @Override
        public String toString() {
            return name;
        }
    }

}

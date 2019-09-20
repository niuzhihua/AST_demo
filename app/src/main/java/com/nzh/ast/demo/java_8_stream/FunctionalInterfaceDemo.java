package com.nzh.ast.demo.java_8_stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by 31414 on 2019/9/6.
 * 函数式接口：只包含一个抽象方法的接口 。
 * <p>
 * 只要接口中仅仅包含一个抽象方法，我们就可以将其改写为 Lambda 表达式。
 * 为了保证一个接口明确的被定义为一个函数式接口（Functional Interface），
 * 我们需要为该接口添加注解：@FunctionalInterface。
 * 这样，一旦你添加了第二个抽象方法，编译器会立刻抛出错误提示
 */

public class FunctionalInterfaceDemo {

    /**
     * 引用类的构造器,普通方法 ,静态方法
     */
    public void common() {
        // 引用类的构造器
        PersonFactory factory = Person::new;
        Person p = factory.create("tom");

        // 引用普通方法
        Converter<String> converter = p::sayHello;
        String s = converter.cc("jerry");

        // 引用静态方法

        StaticConvert<Integer, String> staticConvert = String::valueOf;
        String s2 = staticConvert.convert(45);
    }

    /**
     * 函数式接口  Predicate<T> : 是一个指定入参类型，并返回 boolean 值的函数式接口.用来组合一个复杂的逻辑判断.
     */
    public void testPredicate() {
        Predicate<String> predicate = s -> s.contains("abc");
        Predicate<Integer> predicate2 = i -> i < 100;
        Predicate<List<String>> predicate3 = list -> list.contains("rng");
        Predicate<List<String>> predicate4 = list -> list.contains("ig");


        System.out.println("true 结果:" + predicate.test("abcdefg"));

        System.out.println("false 结果:" + predicate2.negate().test(45));

        List<String> list = new ArrayList<>();
        list.add("edg");
        list.add("rng");
        list.add("tes");
        list.add("ig");

        System.out.println("true 结果:" + predicate3.and(predicate4).test(list));
    }


    /**
     * 函数式接口 Function<Input,Result> :
     */
    public void testFunction() {

        Function<Integer, String> int2String = String::valueOf;
        Function<String, Double> string2Double = Double::valueOf;

        // 将 int2String 的输入类型Integer   和 string2Int的 返回类型  组合
        Function<Integer, Double> f = int2String.andThen(string2Double);

        double result = f.apply(3);   // 结果： 输入int ,返回double

        System.out.println(result);
    }

    /**
     * 函数式接口Supplier : 用于生成一个无构造参数对象。
     */
    public void testSupplier() {

        Supplier<Random> personSupplier = Random::new;
        Random r = personSupplier.get();   // new Person
        System.out.println(r.nextInt());
    }

    /**
     * 函数式接口Supplier : Consumer . 用来被消费
     */
    public void testConsumer() {

        Consumer<Person> consumer = p -> p.sayHello("world");

        consumer.accept(new Person()); // 会执行：new Person().sayHello("world");

    }

    /**
     * 防止空指针的Optional .用 Optional 来包装 对象
     */
    public void testOptional(String str) {

        Optional<String> optional = Optional.of(str);
        optional.isPresent();           // true : 表示 不为 null
        optional.get();
        optional.orElse("default value");
        optional.ifPresent(s -> System.out.println(s));

    }


    // 函数式接口
    @FunctionalInterface
    interface StaticConvert<PARAM, RESULT> {
        RESULT convert(PARAM p);
    }

    // 函数式接口
    @FunctionalInterface
    interface Converter<T> {
        T cc(T t);
    }

    // 函数式接口
    // Person
    interface PersonFactory<P extends Person> {
        P create(String str);
    }

    class Person {
        private String s;

        public Person() {
        }

        public Person(String s) {
            this.s = s;
        }

        public String sayHello(String s) {
            return "hello" + s;
        }


    }
}

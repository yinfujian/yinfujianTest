package com.example.base.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamDemo1 {

    public static void main(String[] args) {
        List<Author> authors = getAuthors();

//        authors.stream().filter(author -> author.getAge() > 13).forEach(System.out::println);
//        authors.stream()
//                .map(Author::getName)
//                .forEach(System.out::println);
//
//        authors.stream()
//                .map(Author::getName)
//                .distinct()
//                .forEach(System.out::println);
//
//        authors.stream().sorted(Comparator.comparing(Author::getAge).reversed()).forEach(System.out::println);
//
//        authors.stream().distinct().limit(2).forEach(System.out::println);
//        authors.stream().distinct().skip(2).forEach(System.out::println);
//
//        authors.stream()
//                .flatMap(author -> author.getBooks().stream())
//                .distinct()
//                .forEach(System.out::println);
//        authors.stream()
//                .flatMap(author -> author.getBooks().stream())
//                .distinct()
//                .flatMap(book -> Arrays.stream(book.getCategory().split(",")))
//                .distinct()
//                .forEach(System.out::println);
//        authors.stream().map(author -> author.getName()).distinct().forEach(System.out::println);
//
//        Optional<Integer> max = authors.stream()
//                .flatMap(author -> author.getBooks().stream())
//                .map(Book::getScore)
//                . max(Comparator.comparingInt(s -> s));
//        System.out.println(max.get());

//        List<String> list = authors.stream()
//                .map(Author::getName)
//                .collect(Collectors.toList());
//        list.stream().forEach(System.out::println);
//        Set<Book> collect = authors.stream()
//                .flatMap(author -> author.getBooks().stream())
//                .collect(Collectors.toSet());

//        Map<String, List<Book>> collect = authors.stream()
//                .collect(Collectors.toMap(Author::getName, Author::getBooks));

//        boolean b = authors.stream().anyMatch(author -> author.getAge() > 29);
//        System.out.println(b);
//        boolean b = authors.stream().allMatch(author -> author.getAge() > 18);
//
//        boolean b1 = authors.stream().noneMatch(author -> author.getAge() >= 100);
//        Optional<Author> any = authors.stream().filter(author -> author.getAge() > 18).findAny();
//        Optional<Author> first = authors.stream()
//                .sorted(Comparator.comparing(Author::getAge))
//                .findFirst();
//        Integer reduce = authors.stream().map(Author::getAge).reduce(0, Integer::sum);
//        System.out.println(reduce);
        Integer reduce = authors.stream()
                .map(Author::getAge)
                .reduce(Integer.MIN_VALUE, ((integer, integer2) -> integer2 > integer ? integer2 : integer));
        System.out.println(reduce);

        authors.stream().map(author -> author.getAge()).map(String::valueOf);
    }

    private static List<Author> getAuthors() {
        // 数据初始化
        Author author = new Author(1L, "蒙多", 33, "一个从菜刀中明悟哲理的祖安人", null);
        Author author2 = new Author(2L, "亚拉索", 15, "狂风也追逐不上他的思考速度", null);
        Author author3 = new Author(3L, "路", 14, "一个从菜刀中明悟哲理的祖安人", null);
        Author author4 = new Author(4L, "路 ", 14, "一个从菜刀中明悟哲理的祖安人", null);

        List<Book> books1 = new ArrayList<>();
        List<Book> books2 = new ArrayList<>();
        List<Book> books3 = new ArrayList<>();
        books1.add(new Book(1L, "author1", "哲学,爱情", 88, ""));
        books1.add(new Book(2L, "author1", "哲学,爱情", 90, ""));

        books2.add(new Book(3L, "author1", "哲学,爱情", 99, ""));
        books2.add(new Book(4L, "author1", "哲学,爱情", 109, ""));
        books2.add(new Book(5L, "author1", "哲学,爱情", 88, ""));

        books3.add(new Book(6L, "author1", "哲学,爱情", 88, ""));
        books3.add(new Book(7L, "author1", "哲学,爱情", 229 , ""));
        books3.add(new Book(8L, "author1", "哲学,爱情", 88, ""));

        author.setBooks(books1);
        author2.setBooks(books2);
        author3.setBooks(books3);
        author4.setBooks(books3);
        List<Author> authors = new ArrayList<>(Arrays.asList(author, author2, author3, author4));
        return authors;
    }
}

package com.example.base.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OptionalDemo {
    public static void main(String[] args) {
//        Author author = getAuthors();
//        Optional<Author> optionalAuthor = Optional.ofNullable(author);
//        optionalAuthor.ifPresent(author1 -> System.out.println(author1.getName()));
//        optionalAuthor.get();
        getAuthors().filter(author -> author.getAge() > 50)
                .ifPresent(author -> System.out.println(author.getName()));
        System.out.println(111);
    }

    private static Optional<Author> getAuthors() {
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
        return Optional.ofNullable(author);
    }
}

package com.example.base.function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Book {

    private Long id;
    private String name;
    private String category;
    private Integer score;
    private String intro;

}

package com.niftyengineer.niftymeals.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "meal")
@Data
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "quantity_available")
    private int quantity_available;

    @Column(name = "category")
    private String category;

    @Column(name = "img")
    private String img;
}

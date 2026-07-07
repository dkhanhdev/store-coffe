package com.verona.cafe.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cafe_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tableNumber;

    @Column(nullable = false)
    private Integer seatCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status;
}

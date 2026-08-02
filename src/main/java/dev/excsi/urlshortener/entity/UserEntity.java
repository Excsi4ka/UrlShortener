package dev.excsi.urlshortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    protected UserEntity() {}

    public Long getId() {
        return id;
    }
}

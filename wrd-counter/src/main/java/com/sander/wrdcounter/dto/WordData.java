package com.sander.wrdcounter.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "word_data")
public class WordData {
    @Id
    private String id;
    private String words;

    public WordData(){}

    public WordData(String id, String words) {
        this.id = id;
        this.words = words;
    }
}

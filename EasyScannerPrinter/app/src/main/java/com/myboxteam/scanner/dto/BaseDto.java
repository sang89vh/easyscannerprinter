package com.myboxteam.scanner.dto;

import java.util.Date;

/**
 * Created by jack on 5/1/16.
 */
public class BaseDto {
    private long id;

    public BaseDto() {
        this.id = new Date().getTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

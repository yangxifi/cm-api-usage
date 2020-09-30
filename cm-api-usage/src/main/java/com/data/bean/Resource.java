package com.data.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private String key;//cpu mem disk
    private Object value;
    private String unit;//单位  num  GB  GB
}

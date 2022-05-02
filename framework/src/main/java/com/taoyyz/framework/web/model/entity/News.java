package com.taoyyz.framework.web.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 22:27
 */
@Data
@Accessors(chain = true)
public class News {
    private String newsTitle;
    private String newsUrl;
    private String newsDate;
}

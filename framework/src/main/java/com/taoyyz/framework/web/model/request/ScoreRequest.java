package com.taoyyz.framework.web.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 5:26
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreRequest {
    @NotNull
    @PositiveOrZero
    private Long tbId;

    @Range(min = 0, max = 100, message = "平时分数有误")
    private String dailyScore;
    @Range(min = 0, max = 100, message = "考试分数有误")
    private String examScore;
    @Range(min = 0, max = 100, message = "总分数有误")
    private String totalScore;
}

package com.munjie.omni.pojo.entity;

import lombok.Data;

/**
 * @Date 2023/11/22 16:21
 * @Author mwj
 **/
@Data
public class ScoreData {
    private String subject;
    private String classroom;
    private int examCount;
    private int actualExamCount;
    private String teacher;

    public ScoreData(String math, String classA, int i, int i1, String teacherA) {
    }
}

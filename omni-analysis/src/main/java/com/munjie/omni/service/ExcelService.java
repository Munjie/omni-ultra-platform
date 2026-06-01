package com.munjie.omni.service;


import com.munjie.omni.pojo.entity.QuestionAnalysisEntity;
import com.munjie.omni.pojo.entity.ScoreEntity;

import java.io.File;
import java.util.List;

/**
 * @Description: TODO
 * @author: Munjie
 * @date: 2023/12/27日 22:17
 */
public interface ExcelService {

     File createExcelFile(List<ScoreEntity> list, List<QuestionAnalysisEntity> entityList, String title);




}

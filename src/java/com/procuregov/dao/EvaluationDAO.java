package com.procuregov.dao;

import com.procuregov.model.EvaluationScore;
import java.util.List;

public interface EvaluationDAO {
    
    EvaluationScore findByBidAndEvaluator(int bidId, int evaluatorId);
    
    List<EvaluationScore> findByTenderId(int tenderId);
    
    List<EvaluationScore> findByEvaluatorId(int evaluatorId);
    
    int insert(EvaluationScore score);
    
    boolean update(EvaluationScore score);
    
    List<Integer> getBidIdsForTender(int tenderId);
    
    int countDistinctEvaluatorsForTender(int tenderId);
    
    boolean hasEvaluatorSubmitted(int bidId, int evaluatorId);
    
    boolean hasAnyEvaluationForTender(int tenderId);
    
    int getEvaluationCountForTender(int tenderId);
}
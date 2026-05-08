package com.procuregov.dao;

import com.procuregov.model.Award;
import java.util.List;

/**
 * Data Access Object interface for Award entity.
 * Provides methods to manage contract awards.
 */
public interface AwardDAO {
    
    Award findByTenderId(int tenderId);
    
    Award findByAwardId(int awardId);
    
    int insert(Award award);
    
    List<Award> findAllAwards();
    
    List<Award> findAwardsByOfficer(int officerId);
    
    boolean updateAward(Award award);
    
    boolean deleteAward(int awardId);
    
    int getTotalAwardedValue();
    
    int getAwardCount();
}
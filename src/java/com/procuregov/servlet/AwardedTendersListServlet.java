package com.procuregov.servlet;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.AwardDAOImpl;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.model.User;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AwardedTendersListServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(AwardedTendersListServlet.class.getName());
    private TenderDAO tenderDAO;
    private AwardDAO awardDAO;
    private BidDAO bidDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        awardDAO = new AwardDAOImpl();
        bidDAO = new BidDAOImpl();
        userDAO = new UserDAOImpl();
        logger.info("AwardedTendersListServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        if (!AuthUtil.requireOfficer(req, resp)) return;
        
        try {
            List<Tender> awardedTenders = tenderDAO.findByStatus("AWARDED");
            if (awardedTenders == null) awardedTenders = new ArrayList<>();
            
            List<Map<String, Object>> awardedDetails = new ArrayList<>();
            
            for (Tender tender : awardedTenders) {
                Award award = awardDAO.findByTenderId(tender.getTenderId());
                if (award != null) {
                    Bid winningBid = bidDAO.findById(award.getWinningBidId());
                    User supplier = null;
                    if (winningBid != null) {
                        supplier = userDAO.findById(winningBid.getSupplierId());
                    }
                    
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("tender", tender);
                    detail.put("award", award);
                    detail.put("supplier", supplier);
                    awardedDetails.add(detail);
                }
            }
            
            req.setAttribute("awardedDetails", awardedDetails);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/awarded-tenders-list.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("Error in doGet: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/officer/dashboard?error=system");
        }
    }
}
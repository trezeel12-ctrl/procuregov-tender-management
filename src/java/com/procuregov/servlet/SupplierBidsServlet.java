package com.procuregov.servlet;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.service.BidService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SupplierBidsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierBidsServlet.class.getName());
    private BidService bidService;

    @Override
    public void init() throws ServletException {
        TenderDAO tenderDAO = new TenderDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();
        bidService = new BidService(bidDAO, tenderDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!AuthUtil.requireSupplier(req, resp)) {
            return;
        }

        try {
            int supplierId = AuthUtil.getSessionUserId(req);
            List<Bid> myBids = bidService.getBidsBySupplier(supplierId);
            if (myBids == null) myBids = new ArrayList<>();
            
            req.setAttribute("myBids", myBids);
            req.getRequestDispatcher("/WEB-INF/jsp/supplier/my-bids.jsp")
                    .forward(req, resp);
                    
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.login;

import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.SAMLHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBoolean;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Christoph
 */
public class AssertionConsumerService extends HttpServlet {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpSession session = request.getSession();
        String encodedAssertion = request.getParameter("assert");

        try {
            String message = SAMLHelper.decode(encodedAssertion);
            Assertion a = (Assertion) SAMLHelper.unmarshall(message.getBytes());

            for (Attribute at : a.getAttributeStatements().get(0).getAttributes()) {
                if (at.getName().equals("sessId")) {
                    String sessId = ((XSString) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("session_id", sessId);
                    logger.info("sessId: " + sessId);
                }
                if (at.getName().equals("isAdmin")) {
                    boolean isAdmin = ((XSBoolean) at.getAttributeValues().get(0)).getValue().getValue();
                    session.setAttribute("is_admin", isAdmin);
                    logger.info("isAdmin: " + Boolean.toString(isAdmin));
                }
                if (at.getName().equals("username")) {
                    String username = ((XSString) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("user_name", username);
                    logger.info("username: " + username);
                }
                if (at.getName().equals("id")) {
                    Integer id = ((XSInteger) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("user_id", id.longValue());
                    logger.info("id: " + Integer.toString(id));
                }
                if (at.getName().equals("roles")) {
                    String role = ((XSString) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("collaboration_user_role", role);
                    logger.info("role: " + role);
                }
                if (at.getName().equals("collab_name")) {
                    String username = ((XSString) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("collaboration username", username);
                    logger.info("username: " + username);
                }
                if (at.getName().equals("collab_id")) {
                    Integer id = ((XSInteger) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("collaboration_user_id", id.longValue());
                    logger.info("id: " + Integer.toString(id));
                }
            }

            if (session.getAttribute("user_id") != null) {
                String lastreq = (String) session.getAttribute("lastreq");
                logger.info("letzter request: " + lastreq);
                
                String weblink = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("weblink", null, null);
                String[] pureLink = weblink.split("/TermBrowser");
                response.sendRedirect(lastreq == null ? "" : pureLink[0] + lastreq);
            }

        } catch (DataFormatException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnmarshallingException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            java.util.logging.Logger.getLogger(AssertionConsumerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

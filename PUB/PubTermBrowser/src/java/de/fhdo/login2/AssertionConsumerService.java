/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.login2;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Organisation;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import de.fhdo.helper.CollabUserRoleHelper;
import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.SAMLHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
 * @author Christoph Kösner
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
            logger.info("begin assertion consumption");
            for (Attribute at : a.getAttributeStatements().get(0).getAttributes()) {
                if (at.getName().equals("sessId")) {
                    String sessId = ((XSString) at.getAttributeValues().get(0)).getValue();
                    session.setAttribute("session_id", sessId);
                    CollaborationSession.getInstance().setPubSessionID(sessId);
                    logger.info("sessId: " + sessId);
                }
                if (at.getName().equals("collab_sessId")) {
                    String sessId = ((XSString) at.getAttributeValues().get(0)).getValue();
										session.setAttribute("collab_session_id", sessId);
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
                if (at.getName().equals("collab_name") && session.getAttribute("collaboration_user_name") == null) {
                    String username = ((XSString) at.getAttributeValues().get(0)).getValue();
										session.setAttribute("collaboration_user_name", username);
                    logger.info("username: " + username);
                }
                if (at.getName().equals("collab_id") && session.getAttribute("collaboration_user_id") == null) {
                    Integer id = ((XSInteger) at.getAttributeValues().get(0)).getValue();
										session.setAttribute("collaboration_user_id", id.longValue());
                    session.setAttribute("CollaborationActive", true);
                    logger.info("id: " + Integer.toString(id));
                }
                if (at.getName().equals("collab_data")) {
                    String collab_data = ((XSString) at.getAttributeValues().get(0)).getValue();
										
                    dataMatching(collab_data, "collab_data");
                    
                    logger.info("collab_data: " + collab_data);
                }
                if (at.getName().equals("term_data")) {
                    String term_data = ((XSString) at.getAttributeValues().get(0)).getValue();
										
                    dataMatching(term_data, "term_data");
                    
                    logger.info("term_data: " + term_data);
                }
            }
            
            String lastreq = (String) session.getAttribute("lastreq");
            logger.info("letzter request: " + lastreq);
            
            String weblink = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("weblink", null, null);
            String[] pureLink = weblink.split("/TermBrowser");
            
            response.sendRedirect(pureLink[0] + lastreq == null ? "/TermBrowser" : lastreq);

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

    private void dataMatching(String data, String type){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Session hb_session = null;
        try{
            logger.info(type);
            if(type.equals("collab_data")){
                String[] data_spl = data.split("\\|");            
                hb_session = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();
                Collaborationuser user = null;
                boolean update = false;

                Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user");
                q.setString("p_user", data_spl[0]);
                java.util.List<Collaborationuser> userList = q.list();

                if(userList.isEmpty()){
                    user = new Collaborationuser();
                    user.getRoles().clear();
                    user.getRoles().add(new Role());
                    user.setOrganisation(new Organisation());
                    update = false;
                }else{
                    user = userList.get(0);
                    update = true;
                }

                //prepare and store            
                user.setUsername(data_spl[0]);
                Role r = (Role)hb_session.get(Role.class, CollabUserRoleHelper.getCollabUserRoleIdByName(data_spl[1]));
                user.getRoles().clear();
                user.getRoles().add(r);

                if(update){
                    user.getOrganisation().setOrganisation(data_spl[8]);
                    user.getOrganisation().setOrganisationAbbr(data_spl[9]);
                }else{
                    user.getOrganisation().setOrganisation(data_spl[8]);
                    user.getOrganisation().setOrganisationAbbr(data_spl[9]);
                    user.getOrganisation().getCollaborationusers().clear();
                    user.getOrganisation().getCollaborationusers().add(user);
                }

                user.setPassword(data_spl[2]);
                user.setSalt(data_spl[3]);
                user.setName(data_spl[4]);
                user.setFirstName(data_spl[5]);
                user.setEmail(data_spl[6]);
                user.setSendMail(Boolean.valueOf(data_spl[7]));
                user.setActivated(Boolean.valueOf(data_spl[10]));
                if(data_spl[11].equals(" ")){
                    user.setActivationTime(new Date());
                }else{
                    user.setActivationTime(sdf.parse(data_spl[11]));
                }
                user.setEnabled(Boolean.valueOf(data_spl[12]));
                user.setHidden(Boolean.valueOf(data_spl[13]));
                user.setDeleted(Boolean.valueOf(data_spl[14]));

                if(update){
                    hb_session.merge(user);
                }else{
                    hb_session.save(user);
                    hb_session.save(user.getOrganisation());
                }    

                hb_session.getTransaction().commit();

            }else if(type.equals("term_data")){
                String[] data_spl = data.split("\\|");   
                hb_session = HibernateUtil.getSessionFactory().openSession();
                hb_session.getTransaction().begin();
                TermUser userTerm = null;
                boolean update = false;

                Query q = hb_session.createQuery("from TermUser WHERE name= :p_user");
                q.setString("p_user", data_spl[0]);
                java.util.List<TermUser> userList = q.list();

                if(userList.isEmpty()){
                    userTerm = new TermUser();
                    update = false;
                }else{
                    userTerm = userList.get(0);
                    update = true;
                }

                //prepare and store
                userTerm.setName(data_spl[0]);
                userTerm.setPassw(data_spl[1]);
                userTerm.setIsAdmin(Boolean.valueOf(data_spl[2]));
                userTerm.setSalt(data_spl[3]);
                userTerm.setEmail(data_spl[4]);
                userTerm.setUserName(data_spl[5]);
                userTerm.setActivated(Boolean.valueOf(data_spl[6]));
                if(data_spl[7].equals(" ")){
                    userTerm.setActivationTime(new Date());
                }else{
                    userTerm.setActivationTime(sdf.parse(data_spl[7]));
                }
                userTerm.setActivationMd5(data_spl[8]);
                userTerm.setEnabled(Boolean.valueOf(data_spl[9]));
                userTerm.setPseudonym(data_spl[10]);

                if(update){
                    hb_session.merge(userTerm);
                }else{
                    hb_session.save(userTerm);
                }    

                hb_session.getTransaction().commit();
            }
        }catch(HibernateException e){
            if(hb_session != null)
                hb_session.getTransaction().rollback();
            logger.info("Assertion unmarshaller: Exception while matching user data!" + e.getMessage());
        } catch (ParseException e) {
            if(hb_session != null)
                hb_session.getTransaction().rollback();
            logger.info("Assertion unmarshaller: Exception while matching user data!" + e.getMessage());
        }finally{
            if(hb_session != null)
                hb_session.close();
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

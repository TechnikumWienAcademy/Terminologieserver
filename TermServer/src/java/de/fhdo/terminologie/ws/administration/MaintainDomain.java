/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Bernhard Rimatzki
 */
public class MaintainDomain
{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    /**
     * Ver�ndert ein bestehende Dom�ne mit den angegebenen Parametern
     * 
     * @param parameter
     * @return Antwort des Webservices
     */
    public MaintainDomainResponseType MaintainDomain(MaintainDomainRequestType parameter)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== MaintainDomain gestartet ======");
        }

        // Return-Informationen anlegen
        MaintainDomainResponseType response = new MaintainDomainResponseType();
        response.setReturnInfos(new ReturnType());

        // Parameter pr�fen
        if (!validateParameter(parameter, response))
        {
            return response; // Fehler bei den Parametern
        }

        // Login-Informationen auswerten (gilt f�r jeden Webservice)
        boolean loggedIn = false;
        LoginInfoType loginInfoType = null;
        if (parameter != null && parameter.getLogin() != null)
        {
            loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
            loggedIn = loginInfoType != null;
            if(loggedIn){
            
                if(loginInfoType.getTermUser().isAdmin()){
                    loggedIn = true;
                }else{
                    loggedIn = false;
                }
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Benutzer ist eingeloggt: " + loggedIn);

        if (loggedIn == false)
        {
            // Benutzer muss f�r diesen Webservice eingeloggt sein
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Sie m�ssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu k�nnen.");
            return response;
        }

        try
        {
            // Hibernate-Block, Session �ffnen
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();

            // Domain und DomainValue zum Speichern vorbereiten
            Domain d = parameter.getDomain();
            Set<DomainValue> dv = parameter.getDomain().getDomainValues();

            String warnString = "d.id: ";
            Long Id = d.getDomainId();
                
            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                //Anpassen der ver�nderten Attribute
                Domain d_db = (Domain)hb_session.get(Domain.class, d.getDomainId());
                if(d.getDomainName() != null && d.getDomainName().length() > 0) 
                    d_db.setDomainName(d.getDomainName());
                if(d.getDisplayText() != null && d.getDisplayText().length() > 0) 
                    d_db.setDisplayText(d.getDisplayText());
                if(d.getDomainOid() != null && d.getDomainOid().length() > 0) 
                    d_db.setDomainOid(d.getDomainOid());
                if(d.getDescription() != null && d.getDescription().length() > 0) 
                    d_db.setDescription(d.getDescription());
                if(d.getIsOptional() != null) 
                    d_db.setIsOptional(d.getIsOptional());
                if(d.getDefaultValue() != null && d.getDefaultValue().length() > 0) 
                    d_db.setDefaultValue(d.getDefaultValue());
                if(d.getDomainType() != null && d.getDomainType().length() > 0) 
                    d_db.setDomainType(d.getDomainType());
                if(d.getDisplayOrder() != null) 
                    d_db.setDisplayOrder(d.getDisplayOrder());
                    
                // Domain in der Datenbank updaten
                hb_session.update(d_db);
                
                //TODO was soll geschehen, wenn sich die Values in einer Dom�ne �ndern. sprich: wenn values rausfallen bzw hinzukommen
                Iterator<DomainValue> idv = dv.iterator();
                warnString = "dv.id: ";
                while (idv.hasNext())
                {
                    DomainValue dvItem = idv.next();
                    Id = dvItem.getDomainValueId();
                    
                    //Anpassen der ver�nderten Attribute
                    DomainValue dv_db = (DomainValue)hb_session.get(DomainValue.class, dvItem.getDomainValueId());
                    if(dvItem.getDomainCode() != null && dvItem.getDomainCode().length() > 0) 
                        dv_db.setDomainCode(dvItem.getDomainCode());
                    if(dvItem.getDomainDisplay() != null && dvItem.getDomainDisplay().length() > 0) 
                        dv_db.setDomainDisplay(dvItem.getDomainDisplay());
                    if(dvItem.getAttribut1classname() != null && dvItem.getAttribut1classname().length() > 0) 
                        dv_db.setAttribut1classname(dvItem.getAttribut1classname());
                    if(dvItem.getAttribut1value() != null && dvItem.getAttribut1value().length() > 0) 
                        dv_db.setAttribut1value(dvItem.getAttribut1value());
                    if(dvItem.getOrderNo() != null) 
                        dv_db.setOrderNo(dvItem.getOrderNo());
                    if(dvItem.getImageFile() != null && dvItem.getImageFile().length() > 0) 
                        dv_db.setImageFile(dvItem.getImageFile());
                    
                    // DomainValue in der Datenbank updaten
                    hb_session.update(dv_db);
                } 

                hb_session.getTransaction().commit();
                
            } catch (Exception e)
            {
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                String message = "Fehler bei 'MaintainDomain', Hibernate: " + e.getLocalizedMessage();
                response.getReturnInfos().setMessage(message);

                logger.error(message);
                // Änderungen nicht erfolgreich
                logger.warn("[MaintainDomain.java] Änderungen nicht erfolgreich, " + warnString + "" + Id);

                hb_session.getTransaction().rollback();
            } finally
            {
                hb_session.close();
            }
            
        }catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'MaintainDomain': " + e.getLocalizedMessage());

            logger.error("Fehler bei 'MaintainDomain': " + e.getLocalizedMessage());
        }

        return response;
    }

    /**
     * Pr�ft die Parameter anhand der Cross-Reference
     * 
     * @param Request
     * @param Response
     * @return false, wenn fehlerhafte Parameter enthalten sind
     */
    private boolean validateParameter(MaintainDomainRequestType Request, MaintainDomainResponseType Response)
    {
        boolean erfolg = true;

        Domain domain = Request.getDomain();
        if (domain == null)
        {
            Response.getReturnInfos().setMessage("Domain darf nicht NULL sein!");
            erfolg = false;
        } else
        {
            // Hibernate-Block, Session �ffnen
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();
            //Ist die mitgegebene ID leer?
						
						try{
							if (domain.getDomainId() == null || domain.getDomainId() == 0)
							{
									Response.getReturnInfos().setMessage(
													"Es muss eine ID f�r die Domain angegeben sein!");
									erfolg = false;
							}
							//Ist die mitgegebene ID in der Datenhaltung vorhanden?
							else if(hb_session.get(Domain.class, domain.getDomainId()) == null)
							{
									Response.getReturnInfos().setMessage(
											"Die angegebene ID existiert f�r die Domain nicht!");
									erfolg = false;
							}

							Set<DomainValue> dvSet = domain.getDomainValues();
							if (dvSet != null)
							{
									Iterator idv = dvSet.iterator();
									while (idv.hasNext())
									{
											DomainValue dvItem = (DomainValue) idv.next();
											//Ist die mitgegebene ID leer?
											if (dvItem.getDomainValueId() == null || dvItem.getDomainValueId() == 0)
											{
													Response.getReturnInfos().setMessage(
																	"Es muss eine ID f�r den Domain-Value angegeben sein!");
													erfolg = false;
											}
											//Ist die mitgegebene ID in der Datenhaltung vorhanden?
											else if(hb_session.get(DomainValue.class, dvItem.getDomainValueId()) == null)
											{
													Response.getReturnInfos().setMessage(
															"Die angegebene ID existiert f�r den Domain-Value nicht!");
													erfolg = false;
											}
											//Geh�rt die mitgegebene ID des Values �berhaupt zu der Domain?
											else if(((DomainValue)hb_session.get(DomainValue.class, dvItem.getDomainValueId())).getDomain().getDomainId()!=domain.getDomainId())
											{
													Response.getReturnInfos().setMessage(
															"Das angegebene Domain-Value geh�rt nicht zu der Domain");
													erfolg = false;
											}
									}
							}
						} catch (Exception e){
							logger.error("Fehler in MaintainDomain: " + e);
							
						} finally {
							hb_session.close();
						}
            
            //hb_session.getTransaction().commit();
            
        }

        if (erfolg == false)
        {
            Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }

        return erfolg;
        
    }
}

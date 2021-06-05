/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.db;

import org.hibernate.Session;

/**
 *
 * @author Christoph Kösner
 */
public class DBSysParamAdmin extends DBSysParam {

    // Singleton-Muster

    private static DBSysParam instance = null;

    public DBSysParamAdmin() {
        super(HibernateUtil.getSessionFactory(HibernateUtil.TERM_ADMIN));
    }

    public static DBSysParam instance() {
        return instance == null ? instance = new DBSysParamAdmin() : instance;
    }
}

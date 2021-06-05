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
package de.fhdo.gui.admin.modules.collaboration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Philipp Urbauer
 */
public class ReportingKollabData {
    
    //Header-Bezeichner
    public static final String GROUP_NAME_Header = "Name der Gruppe";
    public static final String CUSTODIAN_NAME_Header = "Verantwortlicher";
    public static final String NUMBER_PARTICIPANTS_Header = "#Teilnehmer";
    public static final String NUMBER_DISCUSSIONS_Header = "#Diskussionen";
    public static final String NUMBER_CodeSystems_Header = "#Code Systeme";
    public static final String NUMBER_ValueSets_Header = "#Value Sets";
    public static final String NUMBER_Concepts_Header = "#Konzepte";
    public static final String NUMBER_ConMemberships_Header = "#Konzept-Memberships";
    public static final String NUMBER_ACTIVITIES_PreHeader = "#Aktivitäten ";
    
    private String groupName;
    private String custodianName;
    private String numberParticipants;
    private String numberDiskussions;
    private String numberCodeSystems;
    private String numberValueSets;
    private String numberConcepts;
    private String numberConMemberships;
    private ArrayList<String> activitiesPerYear = new ArrayList<String>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCustodianName() {
        return custodianName;
    }

    public void setCustodianName(String custodianName) {
        this.custodianName = custodianName;
    }

    public String getNumberParticipants() {
        return numberParticipants;
    }

    public void setNumberParticipants(String numberParticipants) {
        this.numberParticipants = numberParticipants;
    }

    public String getNumberDiskussions() {
        return numberDiskussions;
    }

    public void setNumberDiskussions(String numberDiskussions) {
        this.numberDiskussions = numberDiskussions;
    }

    public String getNumberCodeSystems() {
        return numberCodeSystems;
    }

    public void setNumberCodeSystems(String numberCodeSystems) {
        this.numberCodeSystems = numberCodeSystems;
    }

    public String getNumberValueSets() {
        return numberValueSets;
    }

    public void setNumberValueSets(String numberValueSets) {
        this.numberValueSets = numberValueSets;
    }

    public String getNumberConcepts() {
        return numberConcepts;
    }

    public void setNumberConcepts(String numberConcepts) {
        this.numberConcepts = numberConcepts;
    }

    public String getNumberConMemberships() {
        return numberConMemberships;
    }

    public void setNumberConMemberships(String numberConMemberships) {
        this.numberConMemberships = numberConMemberships;
    }

    public ArrayList<String> getActivitiesPerYear() {
        return activitiesPerYear;
    }

    public void setActivitiesPerYear(ArrayList<String> activitiesPerYear) {
        this.activitiesPerYear = activitiesPerYear;
    }
}

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
package de.fhdo.collaboration.db;


/**
 *
 * @author Robert Mützner
 */
public enum PO_CLASSNAME
{
  CODESYSTEM_CONCEPT("CodeSystemConcept", "Konzept"),
  CODESYSTEM("CodeSystem", "Code System"),//Vorher: Vokabular
  VALUESET("ValueSet", "Value Set"),
  VALUESET_VERSION("ValueSetVersion","Value Set Version"),
  RELATION("Relation", "Beziehung"),
  CODESYSTEM_VERSION("CodeSystemVersion", "Code System Version"), //Vorher: Vokabular-Version
  ASSOCIATION("CodeSystemEntityVersionAssociation", "Assoziation"),
  CONCEPT_VALUESET_MEMBERSHIP("ConceptValueSetMembership","Konzept Value Set Zugehörigkeit")
  ;
  
  private final String code;
  private final String bezeichnung;

  private PO_CLASSNAME(String Code, String Bezeichnung)
  {
    this.code = Code;
    this.bezeichnung = Bezeichnung;
  }

  public String code()
  {
    return code;
  }

  public String bezeichnung()
  {
    return bezeichnung;
  }

  public static PO_CLASSNAME get(String Code)
  {
    PO_CLASSNAME[] values = PO_CLASSNAME.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].code().equals(Code))
        return values[i];
    }
    return PO_CLASSNAME.CODESYSTEM_CONCEPT;
  }
  
  public static String getString(String Code)
  {
    PO_CLASSNAME ct = get(Code);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}

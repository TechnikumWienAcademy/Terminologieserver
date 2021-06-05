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
package de.fhdo.terminologie.ws.types;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ValueSet;

/**
 *
 * @author Philipp Urbauer
 */
public class DeleteInfo
{
    public enum Type { CODE_SYSTEM, CODE_SYSTEM_VERSION, VALUE_SET, VALUE_SET_VERSION, CODE_SYSTEM_ENTITY_VERSION};

    private Type type;
    private CodeSystem codeSystem;
    private ValueSet valueSet;
    private CodeSystemEntityVersion codeSystemEntityVersion;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CodeSystem getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(CodeSystem codeSystem) {
        this.codeSystem = codeSystem;
    }

    public ValueSet getValueSet() {
        return valueSet;
    }

    public void setValueSet(ValueSet valueSet) {
        this.valueSet = valueSet;
    }

    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return codeSystemEntityVersion;
    }

    public void setCodeSystemEntityVersion(CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
}

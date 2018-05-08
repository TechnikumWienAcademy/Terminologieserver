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

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SearchType
{
  private Boolean traverseConceptsToRoot;
  private Boolean wholeWords, caseSensitive, startsWith;

 
  /**
   * @return the wholeWords
   */
  public Boolean getWholeWords()
  {
    return wholeWords;
  }

  /**
   * @param wholeWords the wholeWords to set
   */
  public void setWholeWords(Boolean wholeWords)
  {
    this.wholeWords = wholeWords;
  }

  /**
   * @return the caseSensitive
   */
  public Boolean getCaseSensitive()
  {
    return caseSensitive;
  }

  /**
   * @param caseSensitive the caseSensitive to set
   */
  public void setCaseSensitive(Boolean caseSensitive)
  {
    this.caseSensitive = caseSensitive;
  }

  /**
   * @return the startsWith
   */
  public Boolean getStartsWith()
  {
    return startsWith;
  }

  /**
   * @param startsWith the startsWith to set
   */
  public void setStartsWith(Boolean startsWith)
  {
    this.startsWith = startsWith;
  }

  /**
   * @return the traverseConceptsToRoot
   */
  public Boolean getTraverseConceptsToRoot()
  {
    return traverseConceptsToRoot;
  }

  /**
   * @param traverseConceptsToRoot the traverseConceptsToRoot to set
   */
  public void setTraverseConceptsToRoot(Boolean traverseConceptsToRoot)
  {
    this.traverseConceptsToRoot = traverseConceptsToRoot;
  }
  
  
}

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
package de.fhdo.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Robert Mützner
 */
public class DateTimeHelper
{
  public static String ConvertXMLGregorianCalenderToString(XMLGregorianCalendar Calendar)
  {
    return ConvertXMLGregorianCalenderToString(Calendar, "dd.MM.yyyy");
  }

  public static String ConvertXMLGregorianCalenderToString(
          XMLGregorianCalendar Calendar, String Format)
  {
    if (Calendar == null)
      return "";

    SimpleDateFormat sdf = new SimpleDateFormat(Format);
    GregorianCalendar gregorianCalendar = Calendar.toGregorianCalendar();
    return sdf.format(gregorianCalendar.getTime());
  }

  public static long GetDateDiffInDays(XMLGregorianCalendar Calendar)
  {
    if (Calendar == null)
      return -1;

    /** The date at the end of the last century */
    Date d1 = Calendar.toGregorianCalendar().getTime();

    /** Today's date */
    Date today = new Date();

    if(today.after(d1))
      return -1;

    // Get msec from each, and subtract.
    long diff = d1.getTime() - today.getTime();

    return (diff / (1000 * 60 * 60 * 24)) + 1;
    //System.out.println("The 21st century (up to " + today + ") is "
    //    + (diff / (1000 * 60 * 60 * 24)) + " days old.");
  }

  public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date)
  {
    GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
    gc.setTime(date);
    DatatypeFactory dataTypeFactory = null;
    try
    {
      dataTypeFactory = DatatypeFactory.newInstance();
    }
    catch (DatatypeConfigurationException ex)
    {
      ex.printStackTrace();
      //Logger.getLogger(InstallmentBean.class.getName()).log(Level.SEVERE, null, ex);
    }
    XMLGregorianCalendar value = dataTypeFactory.newXMLGregorianCalendar(gc);
    return value;
  }
}

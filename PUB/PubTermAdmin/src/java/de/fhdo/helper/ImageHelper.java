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

/**
 *
 * @author Robert Mützner
 */
public class ImageHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static String getImageSrcFromMimeType(String MimeType)
  {
    String s_icon = "";

    if(MimeType == null || MimeType.length() == 0)
      return "/rsc/img/filetypes/white.png";


    if (MimeType.equals("licence"))
    {
      s_icon = "/rsc/img/filetypes/licence.png";
    }
    else if(MimeType.contains("image"))
    {
      s_icon = "/rsc/img/filetypes/picture.png";
    }
    else if(MimeType.contains("video"))
    {
      s_icon = "/rsc/img/filetypes/movie.png";
    }
    else if (MimeType.equals("application/msword") ||
             MimeType.equals("application/vnd.ms-word") ||
             MimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
    {
      s_icon = "/rsc/img/filetypes/word.png";
    }
    else if (MimeType.equals("application/msexcel") ||
             MimeType.equals("application/vnd.ms-excel") ||
             MimeType.equals("application/excel") ||
             MimeType.equals("application/x-ms-excel") ||
             MimeType.equals("application/x-msexcel") ||
             MimeType.equals("application/xls") ||
             MimeType.equals("application/xlsx") ||
             MimeType.equals("application/csv") ||
             MimeType.equals("text/csv") ||
             MimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    {
      s_icon = "/rsc/img/filetypes/excel.png";
    }
    else if (MimeType.equals("application/pdf"))
    {
      s_icon = "/rsc/img/filetypes/acrobat.png";
    }
    else if (MimeType.equals("application/vnd.ms-powerpoint") ||
             MimeType.equals("application/ms-powerpoint") ||
             MimeType.equals("application/mspowerpoint") ||
             MimeType.equals("application/x-powerpoint"))
    {
      s_icon = "/rsc/img/filetypes/powerpoint.png";
    }

    else if (MimeType.equals("application/x-zip-compressed"))
    {
      s_icon = "/rsc/img/filetypes/zip.png";
    }
    else if (MimeType.equals("text/plain"))
    {
      s_icon = "/rsc/img/filetypes/text.png";
    }
    else
    {
      s_icon = "/rsc/img/filetypes/white.png";
    }
    
    logger.debug("Icon from Mimetype '" + MimeType + "': " + s_icon);

    return s_icon;
  }

  public static boolean isImage(String MimeType)
  {
    if (MimeType.contains("image") && MimeType.equals("image/tiff") == false)
      return true;

    return false;
  }
}

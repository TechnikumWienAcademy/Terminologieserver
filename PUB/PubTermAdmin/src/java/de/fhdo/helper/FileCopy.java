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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import org.zkoss.util.media.Media;

public class FileCopy
{

  public static Reader asReader(Media media)
  {
    return new BufferedReader(media.inMemory() ? new StringReader(media.getStringData()) : media.getReaderData());
  }

  public static void copy(String fromFileName, String toFileName)
    throws IOException
  {
    File fromFile = new File(fromFileName);
    File toFile = new File(toFileName);

    if (!fromFile.exists())
      throw new IOException("FileCopy: " + "no such source file: "
        + fromFileName);
    if (!fromFile.isFile())
      throw new IOException("FileCopy: " + "can't copy directory: "
        + fromFileName);
    if (!fromFile.canRead())
      throw new IOException("FileCopy: " + "source file is unreadable: "
        + fromFileName);

    if (toFile.isDirectory())
      toFile = new File(toFile, fromFile.getName());

    if (toFile.exists())
    {
      if (!toFile.canWrite())
        throw new IOException("FileCopy: "
          + "destination file is unwriteable: " + toFileName);
      System.out.print("Overwrite existing file " + toFile.getName()
        + "? (Y/N): ");
      System.out.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(
        System.in));
      String response = in.readLine();
      if (!response.equals("Y") && !response.equals("y"))
        throw new IOException("FileCopy: "
          + "existing file was not overwritten.");
    }
    else
    {
      String parent = toFile.getParent();
      if (parent == null)
        parent = System.getProperty("user.dir");
      File dir = new File(parent);
      if (!dir.exists())
        throw new IOException("FileCopy: "
          + "destination directory doesn't exist: " + parent);
      if (dir.isFile())
        throw new IOException("FileCopy: "
          + "destination is not a directory: " + parent);
      if (!dir.canWrite())
        throw new IOException("FileCopy: "
          + "destination directory is unwriteable: " + parent);
    }

    FileInputStream from = null;
    FileOutputStream to = null;
    try
    {
      from = new FileInputStream(fromFile);
      to = new FileOutputStream(toFile);
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = from.read(buffer)) != -1)
      {
        to.write(buffer, 0, bytesRead); // write
      }
    }
    finally
    {
      if (from != null)
        try
        {
          from.close();
        }
        catch (IOException e)
        {
        }
      if (to != null)
        try
        {
          to.close();
        }
        catch (IOException e)
        {
        }
    }
  }
}

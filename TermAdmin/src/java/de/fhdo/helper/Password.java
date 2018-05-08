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

import java.security.SecureRandom;

/**
 *
 * @author Robert Mützner
 */
public class Password
{
  public static final int it_count = 1000;

  public Password()
  {

  }

  public static String getSaltedPassword(String Password, String Salt, String Username)
  {
    if(Salt == null || Salt.length() == 0)
      return MD5.getMD5(Password);
      
    String md5 = MD5.getMD5(Password + Salt + Username);
    
    for(int i=0;i<it_count;++i)
    {
      md5 = MD5.getMD5(md5);
    }
    //String md5 = MD5.getMD5(Password);

    return md5;
  }

  public static String generateRandomSalt()
  {
    return generateRandomPassword(12);
  }

  public static String generateRandomPassword(int Length)
  {
    SecureRandom sr = new SecureRandom();
	
		char[] pw = new char[Length];
    int c = 'A';
    int r1 = 0;
    for (int i = 0; i < Length; i++)
    {
      r1 = sr.nextInt(3); //(int) (Math.random() * 3);
      switch (r1)
      {
        case 0:
          c = '0' + sr.nextInt(10);// (Math.random() * 10);
          break;
        case 1:
          c = 'a' + sr.nextInt(26);//(int) (Math.random() * 26);
          break;
        case 2:
          c = 'A' + sr.nextInt(26);//(int) (Math.random() * 26);
          break;
      }
      pw[i] = (char) c;
    }

    return new String(pw);
  }



}

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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;


/**
 *
 * @author Robert Mützner
 */
public class DES
{

  public static String encrypt(String Text)
  {
    try
    {
      DESKeySpec keySpec = new DESKeySpec("schluessel_stdrepository15".getBytes("UTF8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey key = keyFactory.generateSecret(keySpec);

      // ENCODE plainTextPassword String
      byte[] cleartext = Text.getBytes("UTF8");
      Cipher cipher = Cipher.getInstance("DES");
      // cipher is not thread safe
      cipher.init(Cipher.ENCRYPT_MODE, key);
      return Base64.encodeBase64URLSafeString(cipher.doFinal(cleartext));

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

  public static String decrypt(String Text)
  {
    try
    {
      DESKeySpec keySpec = new DESKeySpec("schluessel_stdrepository15".getBytes("UTF8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey key = keyFactory.generateSecret(keySpec);

      byte[] encrypedPwdBytes = Base64.decodeBase64(Text);
      Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
      cipher.init(Cipher.DECRYPT_MODE, key);

      byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));

      return new String(plainTextPwdBytes);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }
}

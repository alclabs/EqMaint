/*
 * Copyright (c) 2010 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.controlj.addon.eqmaint.database;

import java.util.Date;

/**
 A very simple data holder class.  The getXXX() methods are defined (even though
 the fields are public) because it provides a bean-like interface for the web
 pages so that expression syntax works as expected.
 */
public class NoteData
{
   public long id;
   public Date date;
   public String operator;
   public String type;
   public String notes;

   public long getId()
   {
      return id;
   }

   public Date getDate()
   {
      return date;
   }

   public String getOperator()
   {
      return operator;
   }

   public String getType()
   {
      return type;
   }

   public String getNotes()
   {
      return notes;
   }

   @Override public String toString()
   {
      return date+" "+operator+" ("+type+") - "+notes;
   }
}


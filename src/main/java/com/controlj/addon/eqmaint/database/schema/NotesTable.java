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
package com.controlj.addon.eqmaint.database.schema;

import com.controlj.green.addonsupport.xdatabase.DatabaseSchema;
import com.controlj.green.addonsupport.xdatabase.ForeignKey;
import com.controlj.green.addonsupport.xdatabase.TableSchema;
import com.controlj.green.addonsupport.xdatabase.column.DateColumn;
import com.controlj.green.addonsupport.xdatabase.column.IntColumn;
import com.controlj.green.addonsupport.xdatabase.column.LongColumn;
import com.controlj.green.addonsupport.xdatabase.column.StringColumn;

/**
 Simple class for defining the notes table.  Because the fields are
 final, there is not safety probem making them public.  And not having
 accessor method makes code that references these columns much easier
 to read.
 */
public class NotesTable
{
   public final TableSchema schema;
   public final LongColumn id;
   public final StringColumn location;
   public final DateColumn date;
   public final IntColumn operId;
   public final IntColumn typeId;
   public final StringColumn notes;
   public final ForeignKey operFK;
   public final ForeignKey typeFK;

   /**
    This constructor takes the operator and type tables.  This is an easy way
    to establish foreign key constraints, and makes it very obvious from the
    calling code that this table relies on those other tables.
    */
   public NotesTable(DatabaseSchema db, OperatorTable operTable, TypeTable typeTable)
   {
      schema = db.addTable("Notes");
      id = schema.addLongColumn("ID_");
      location = schema.addStringColumn("Location_", 200);
      date = schema.addDateColumn("Date_");
      operId = schema.addIntColumn("OperId_");
      typeId = schema.addIntColumn("TypeId_");
      notes = schema.addStringColumn("Notes_", 2000);

      schema.setPrimaryKey(id);
      schema.setAutoGenerate(id);
      schema.addIndex("LocIdx", location);
      schema.addIndex("LocDateIdx", location, date);
      schema.addIndex("LocOperIdx", location, operId);
      schema.addIndex("LocTypeIdx", location, typeId);
      operFK = schema.addForeignKey("operFK", operId).references(operTable.id);
      typeFK = schema.addForeignKey("typeFK", typeId).references(typeTable.id);
   }

}


/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)TypeTable

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint.database.schema;

import com.controlj.green.addonsupport.xdatabase.DatabaseSchema;
import com.controlj.green.addonsupport.xdatabase.TableSchema;
import com.controlj.green.addonsupport.xdatabase.column.IntColumn;
import com.controlj.green.addonsupport.xdatabase.column.StringColumn;

/**
 Simple class for defining the types table.  Because the fields are
 final, there is not safety probem making them public.  And not having
 accessor method makes code that references these columns much easier
 to read.
 */
public class TypeTable
{
   public final TableSchema schema;
   public final IntColumn id;
   public final StringColumn name;

   public TypeTable(DatabaseSchema db)
   {
      schema = db.addTable("Types");
      id = schema.addIntColumn("ID_");
      name = schema.addStringColumn("Name_", 20);

      schema.setPrimaryKey(id);
      schema.setAutoGenerate(id);
   }
}


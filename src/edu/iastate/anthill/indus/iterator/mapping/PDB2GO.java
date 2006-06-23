package edu.iastate.anthill.indus.iterator.mapping;

import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 2005-04-08
 *
 * ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/PDB/gene_association.goa_pdb.gz

 File format
 ---------------

 gene_association.goa_pdb

 This file contains GO assignments to PDB entries

 We have complied with the file format described by the GeneOntology consortium
 for annotation files (http://www.geneontology.org/GO.annotation.html#file)

 1.  DB
     Database from which entry has been taken.
     ie: 'PDB'

 2.  DB_Object_ID
     A unique identifier in the DB for the item being annotated.
     Here: ID of the PDB entry.
     Example: '1BDL'

 3.  DB_Object_Symbol
     Chain ID of the PDB entry
     Example: 'A'

 4.  NOT
     Always empty.

 5.  GOid
     The GO identifier for the term attributed to the DB_Object_ID.
     Example: 'GO:0005625'

 6.  DB:Reference
     Explains the method used to infer the annotation.
     Always: GOA:interpro

 7.  Evidence
     Always 'IEA'.

 8.  With
     UniProt AC from where the annotation is inferred from.
     Example: 'UniProt:O00341'

 9.  Aspect
 One of the three ontologies: P (biological process), F (molecular function)
     or C (cellular component).
     Example: 'P'

 10. DB_Object_Name
     Always empty

 11. Synonym
     Always empty.

 12. DB_Object_Type
     What kind of entity is being annotated.
     Always 'protein_structue'

 13. Taxon_ID
     Identifier for the species being annotated.
     Example: 'taxon:9606'

 14. Date
     The date of last annotation update in the format 'YYYYMMDD' eg: 20030228

 15. Assigned_By
     Attribute describing the source of the annotation.
    Always 'UniProt'

 */
public class PDB2GO
    extends IndusDB implements MappingDB
{
    // PDB	101M	@		GO:0005488	GOA:interpro	IEA	UniProt:P02185	F			protein_structure	taxon:9755	20050308	UniProt


    public static void main(String[] args)
    {
        PDB2GO pdb2go = new PDB2GO();
    }

    public void loadToDB()
    {
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE pdb2go " +
            "( " +
            "\"DB\"varchar(32), " +
            "\"DB_Object_ID\"varchar(32), " +
            "\"DB_Object_Symbol\"varchar(32), " +
            "\"NOT\"varchar(32), " +
            "\"GOid\"varchar(32), " +
            "\"DB:Reference\"varchar(32), " +
            "\"Evidence\"varchar(32), " +
            "\"With\"varchar(32), " +
            "\"Aspect\"varchar(32), " +
            "\"DB_Object_Name\"varchar(32), " +
            "\"Synonym\"varchar(32), " +
            "\"DB_Object_Type\"varchar(32), " +
            "\"Taxon_ID\"varchar(32), " +
            "\"Date\"varchar(8), " +
            "\"Assigned_By\"varchar(32))";
        return JDBCUtils.updateDatabase(this.db, sql);
    }
}

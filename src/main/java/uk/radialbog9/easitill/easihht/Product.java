package uk.radialbog9.easitill.easihht;

import com.opencsv.bean.CsvBindByName;

/**
 * A minimal implementation of a product on Easitill. <br>
 * Only contains basic data (Linecode, PLU, Description, Internet Text, Cost, Category)
 */
public class Product {
    @CsvBindByName(column = "Linecode", required = true)
    public int linecode;
    @CsvBindByName(column = "PLU")
    public String plu;
    @CsvBindByName(column = "Description", required = true)
    public String description;
    @CsvBindByName(column = "InternetText1")
    public String internetText;
    @CsvBindByName(column = "GrossSalePrice")
    public float cost;
    @CsvBindByName(column = "GroupDescription")
    public String category;
}

package uk.radialbog9.easitill.easihht;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.experimental.Accessors;

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
    @CsvBindByName(column = "StockQuantity")
    public float originalStock;
}

package uk.radialbog9.easitill.easihht;

import com.opencsv.bean.CsvBindByName;

public class StockTakeImportProduct {
    @CsvBindByName(column = "Linecode")
    public int linecode;
    @CsvBindByName(column = "StockQuantity")
    public float stock;
}

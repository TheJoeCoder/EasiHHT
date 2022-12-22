package uk.radialbog9.easitill.easihht;

import com.opencsv.bean.CsvBindByPosition;

public class StockTakeImportProduct {
    @CsvBindByPosition(position = 0)
    public int linecode;
    @CsvBindByPosition(position = 1)
    public float stock;
}

package uk.radialbog9.easitill.easihht;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StockTake {
    @Getter
    private static HashMap<Product, Integer> productStockMap = new HashMap<>();

    private static HashMap<String, Product> pluProductCache = new HashMap<>();
    private static HashMap<String, Product> descPeoductCache = new HashMap<>();

    public static void importProducts(File productFile) throws Exception {
        // Import product data file
        List<Product> productList = new CsvToBeanBuilder(new FileReader(productFile))
                .withType(Product.class).build().parse();
        for(Product product : productList) {
            productStockMap.put(product, 0);
            pluProductCache.put(product.plu, product);
            descPeoductCache.put(product.description, product);
        }
    }

    public static void exportStock() throws IOException {
        StringBuilder fileExportString = new StringBuilder("Linecode,StockQuantity");
        for(Product product : productStockMap.keySet()) {
            int linecode = product.linecode;
            int stock = productStockMap.get(product);
            fileExportString.append("\n").append(linecode).append(",").append(stock);
        }
        FileWriter writer = new FileWriter("stock.csv");
        writer.write(fileExportString.toString());
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        // Parse arguments
        JSAP jsap = new JSAP();

        FlaggedOption opt1 = new FlaggedOption("file")
                .setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeFile(true))
                .setRequired(true)
                .setShortFlag('f')
                .setLongFlag("file")
                .setLongFlag(JSAP.NO_LONGFLAG);
        opt1.setHelp("ProductData.csv file to import");
        jsap.registerParameter(opt1);

        JSAPResult config = jsap.parse(args);
        if (!config.success()) {
            System.err.println();
            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (java.util.Iterator errs = config.getErrorMessageIterator();
                 errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            System.err.println();
            System.err.println("Usage: java "
                    + StockTake.class.getName());
            System.err.println("                "
                    + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }

        // Load product file
        importProducts(config.getFile("file"));

        // Wait for input
        Scanner scanner = new Scanner(System.in);
        // Steps:
        // 0: Product Select
        // 1: Enter stock
        int step = 0;
        Product currentProduct = null;
        try {
            while (true) {
                if(step == 0) System.out.println("Enter PLU or type q to exit");
                if(step == 1) {
                    System.out.println("-= Product " + currentProduct.linecode + " =-");
                    System.out.println(currentProduct.description);
                    System.out.println("PLU:" + currentProduct.plu);
                    System.out.println("Current Stock Take Amount: " + productStockMap.get(currentProduct));
                    System.out.println("Enter new stock or press enter to go back");
                }
                System.out.print("> ");
                String line = scanner.nextLine();
                if(line.equalsIgnoreCase("q")) {
                    // Quit
                    System.out.println("Saving and exiting...");
                    exportStock();
                    System.exit(0);
                }
                else if(step == 0) {
                    // Select by PLU
                    if(pluProductCache.containsKey(line)) {
                        currentProduct = pluProductCache.get(line);
                        step = 1;
                    } else {
                        System.out.println("PLU doesn't exist. Did you mean to search?");
                    }
                } else if(step == 1) {
                    int val = -1;
                    try {
                        val = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {}
                    if(val >= 0) {
                        // Stock change
                        productStockMap.put(currentProduct, val);
                        System.out.println("Stock updated to " + val);
                    } else {
                        // Return
                        System.out.println("Stock not updated");
                    }
                    currentProduct = null;
                    step = 0;
                } else {
                    System.err.println("Something's gone very wrong! Resetting...");
                    step = 0;
                    currentProduct = null;
                }
            }
        } catch(IllegalStateException | NoSuchElementException e) {
            // System.in has been closed
            System.out.println("System.in was closed; exiting");
        }
    }
}

package uk.radialbog9.easitill.easihht;

import com.martiansoftware.jsap.*;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StockTake {
    private static HashMap<Product, Integer> existingProdStockMap = new HashMap<>();
    private static HashMap<Product, Integer> productStockMap = new HashMap<>();

    private static HashMap<String, Product> pluProductCache = new HashMap<>();
    private static HashMap<String, Product> descProductCache = new HashMap<>();
    private static HashMap<Integer, Product> linecodeProductCache = new HashMap<Integer, Product>();

    private static String lineEndings = "\n";

    public static void importProducts(File productFile, boolean zeroStock) throws Exception {
        // Import product data file
        List<Product> productList = new CsvToBeanBuilder(new FileReader(productFile))
                .withType(Product.class).build().parse();
        for(Product product : productList) {
            existingProdStockMap.put(product, Math.round(product.originalStock));
            if(zeroStock) setStock(product, 0);
            if(product.plu != null) pluProductCache.put(product.plu, product);
            descProductCache.put(product.description, product);
            linecodeProductCache.put(product.linecode, product);
        }
    }

    public static void setStock(Product product, int stock) {
        productStockMap.put(product, stock);
    }

    public static int getStock(Product product) {
        if (productStockMap.containsKey(product)) {
            return productStockMap.get(product);
        } else return existingProdStockMap.getOrDefault(product, 0);
    }

    public static void importExistingStockTake(File file) throws Exception {
        List<StockTakeImportProduct> stockTakeImportProductList = new CsvToBeanBuilder(new FileReader(file))
                .withType(StockTakeImportProduct.class).build().parse();
        for(StockTakeImportProduct stProd : stockTakeImportProductList) {
            if(linecodeProductCache.containsKey(stProd.linecode)) {
                setStock(linecodeProductCache.get(stProd.linecode), Math.round(stProd.stock));
            } else {
                //doesn't exist
                System.err.println("Product " + stProd.linecode + " in previous stock take file doesn't exist in product data file.");
            }
        }
    }

    public static void exportStock(File outFile) throws IOException {
        StringBuilder fileExportString = new StringBuilder();
        for(Product product : productStockMap.keySet()) {
            int linecode = product.linecode;
            int stock = productStockMap.get(product);
            fileExportString.append(linecode).append(",").append(stock).append(lineEndings);
        }
        FileWriter writer = new FileWriter(outFile);
        writer.write(fileExportString.toString());
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        // Parse arguments
        JSAP jsap = new JSAP();

        UnflaggedOption opt1 = new UnflaggedOption("file")
                .setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeFile(true))
                .setRequired(true);
        opt1.setHelp("ProductData.csv file to import");
        jsap.registerParameter(opt1);

        Switch sw1 = new Switch("zerostock")
                .setShortFlag('z')
                .setLongFlag("zero");
        sw1.setHelp("Zero stock after importing");
        jsap.registerParameter(sw1);

        Switch sw2 = new Switch("winlineend")
                .setShortFlag('w')
                .setLongFlag("windows");
        sw2.setHelp("Set line endings to Windows (\\r\\n) instead of Unix (\\n)");

        FlaggedOption opt2 = new FlaggedOption("resumetake")
                .setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeFile(true))
                .setShortFlag('r')
                .setLongFlag("resume");
        opt2.setHelp("Continue stock take from specified stock.csv file (overrides -z)");
        jsap.registerParameter(opt2);

        FlaggedOption opt3 = new FlaggedOption("outputfile")
                .setStringParser(FileStringParser.getParser().setMustExist(false).setMustBeFile(true))
                .setShortFlag('o')
                .setLongFlag("output");
        opt3.setHelp("Set output file");
        jsap.registerParameter(opt3);

        JSAPResult config = jsap.parse(args);
        if (!config.success()) {
            System.err.println();
            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (java.util.Iterator errs = config.getErrorMessageIterator();
                 errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            System.err.println();
            System.err.println("Usage: java -jar "
                    + new File(StockTake.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName());
            System.err.println("                "
                    + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }

        lineEndings = config.getBoolean("winlineend", false) ? "\r\n" : "\n";

        // Load product file
        importProducts(config.getFile("file"), config.getBoolean("zerostock"));
        if(config.getFile("resumetake") != null) importExistingStockTake(config.getFile("resumetake"));

        // Wait for input
        Scanner scanner = new Scanner(System.in);
        // Steps:
        // 0: Product Select
        // 1: Enter stock
        int step = 0;
        Product currentProduct = null;
        try {
            while (true) {
                // Output to start each step
                if (step == 0) System.out.println("Enter PLU or type q to exit");
                if (step == 1) {
                    System.out.println("\n\n");
                    System.out.println("Linecode:");
                    System.out.println(currentProduct.description);
                    System.out.println("PLU:" + currentProduct.plu);
                    System.out.println("Current Stock Take Amount: " + getStock(currentProduct));
                    System.out.println("Enter new stock or press enter to go back");
                }

                // Input processing
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("q")) {
                    // Quit
                    System.out.println("Saving and exiting...");
                    exportStock(config.getFile("outputfile", new File("stock.csv")));
                    System.exit(0);
                } /* Input Steps */ else if (step == 0) {
                    // Select by PLU
                    if (pluProductCache.containsKey(line)) {
                        currentProduct = pluProductCache.get(line);
                        step = 1;
                    } else {
                        System.out.println("PLU doesn't exist.");
                    }
                } else {
                    int val = -1;
                    try {
                        val = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {}
                    if (val >= 0) {
                        // Stock change
                        setStock(currentProduct, val);
                        System.out.println("Stock updated to " + val);
                    } else {
                        // Return
                        System.out.println("Stock not updated");
                    }
                    currentProduct = null;
                    step = 0;
                }
            }
        } catch (IllegalStateException | NoSuchElementException e) {
            // System.in has been closed
            System.out.println("Forcibly exiting...");
            System.exit(1);
        }
    }
}

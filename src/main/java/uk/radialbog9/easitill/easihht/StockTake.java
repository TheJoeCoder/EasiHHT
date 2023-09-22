package uk.radialbog9.easitill.easihht;

import com.martiansoftware.jsap.*;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import uk.radialbog9.easitill.easihht.web.WebServer;

import java.io.*;
import java.util.*;

@SuppressWarnings({"rawtypes", "MismatchedQueryAndUpdateOfCollection"})
public class StockTake {
    private static final HashMap<Product, Integer> existingProdStockMap = new HashMap<>();
    private static final HashMap<Product, Integer> productStockMap = new HashMap<>();

    @Getter
    private static final HashMap<String, Product> pluProductCache = new HashMap<>();
    @Getter
    private static final HashMap<String, Product> descProductCache = new HashMap<>();
    @Getter
    private static final HashMap<Integer, Product> linecodeProductCache = new HashMap<>();

    private static String lineEndings = "\n";

    public static void importProducts(File productFile, boolean zeroStock, boolean insertAllExport) throws Exception {
        // Import product data file
        List<Product> productList = new CsvToBeanBuilder<Product>(new FileReader(productFile))
                .withType(Product.class).build().parse();
        for(Product product : productList) {
            existingProdStockMap.put(product, Math.round(product.originalStock));
            if(insertAllExport) setStock(product, Math.round(product.originalStock));
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
        List<StockTakeImportProduct> stockTakeImportProductList = new CsvToBeanBuilder<StockTakeImportProduct>(new FileReader(file))
                .withType(StockTakeImportProduct.class).build().parse();
        for(StockTakeImportProduct stProd : stockTakeImportProductList) {
            if(linecodeProductCache.containsKey(stProd.linecode)) {
                setStock(linecodeProductCache.get(stProd.linecode), Math.round(stProd.stock));
            } else {
                //doesn't exist
                System.err.println(ANSICol.RED + "Product " + stProd.linecode + " in previous stock take file doesn't exist in product data file." + ANSICol.RESET);
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

    private static void graphicalStockTake(File outFile) {
// Wait for input
        Scanner scanner = new Scanner(System.in);
        // Steps:
        // 0: Product Select
        // 1: Enter stock
        int step = 0;
        Product currentProduct = null;
        List<Product> searchResults = new ArrayList<>();
        try {
            while (true) {
                // Output to start each step
                if (step == 0) System.out.println(ANSICol.CYAN + "Enter PLU, search with ?<query> or type q to exit" + ANSICol.RESET);
                if (step == 1) {
                    System.out.println(ANSICol.GREEN + "Linecode:" + currentProduct.linecode);
                    System.out.println(currentProduct.description);
                    System.out.println("PLU:" + currentProduct.plu);
                    System.out.println("Current Stock Take Amount: " + getStock(currentProduct));
                    System.out.println(ANSICol.CYAN + "Enter new stock or press enter to go back" + ANSICol.RESET);
                }
                if (step == 2) {
                    if(searchResults.isEmpty()) {
                        // Skip search if no results
                        System.out.println(ANSICol.RED + "No results found!" + ANSICol.RESET);
                        step = 0;
                        continue;
                    }
                    if(searchResults.size() == 1) {
                        // Skip search if only one result
                        currentProduct = searchResults.get(0);
                        step = 1;
                        continue;
                    }
                    System.out.println(ANSICol.CYAN + "Search results:" + ANSICol.RESET);
                    for (int i = 0; i < searchResults.size(); i++) {
                        Product product = searchResults.get(i);
                        System.out.println(ANSICol.GREEN + "Linecode:" + product.linecode);
                        System.out.println(product.description);
                        System.out.println("PLU:" + product.plu);
                        System.out.println("Current Stock Take Amount: " + getStock(product));
                        System.out.println(ANSICol.CYAN + "Enter " + i + " to select" + ANSICol.RESET);
                        System.out.println();
                    }
                    System.out.println(ANSICol.CYAN + "Enter item or press enter to go back" + ANSICol.RESET);
                }

                // Input processing
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("q")) {
                    // Quit
                    System.out.println("Saving and exiting...");
                    try {
                        exportStock(outFile);
                        System.exit(0);
                    } catch (IOException e) {
                        System.err.println(ANSICol.RED + "Failed to save stock file!" + ANSICol.RESET);
                        System.exit(1);
                    }
                } /* Input Steps */ else if (step == 0) {
                    if (line.startsWith("?")) {
                        String query = line.substring(1);
                        // Search
                        searchResults.clear();
                        for (String productDesc : descProductCache.keySet()) {
                            if (productDesc.toLowerCase().contains(query.toLowerCase())) {
                                searchResults.add(descProductCache.get(productDesc));
                            }
                        }
                        step = 2;
                    } else {
                        // Select by PLU
                        if (pluProductCache.containsKey(line)) {
                            currentProduct = pluProductCache.get(line);
                            step = 1;
                        } else {
                            System.out.println(ANSICol.RED + "PLU doesn't exist.");
                        }
                    }
                } else if (step == 1) {
                    int val = -1;
                    try {
                        val = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {}
                    if (val >= 0) {
                        // Stock change
                        setStock(currentProduct, val);
                        System.out.println(ANSICol.CYAN + "Stock updated to " + val + ANSICol.RESET);
                    } else {
                        // Return
                        System.out.println(ANSICol.RED + "Stock not updated" + ANSICol.RESET);
                    }
                    currentProduct = null;
                    step = 0;
                } else { // if (step == 2)
                    if(line.isBlank()) {
                        // Return
                        System.out.println(ANSICol.RED + "Search cancelled" + ANSICol.RESET);
                        step = 0;
                    }
                    int val = -1;
                    try {
                        val = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {}
                    if (val >= 0 && val < searchResults.size()) {
                        // Select by search
                        currentProduct = searchResults.get(val);
                        step = 1;
                    } else {
                        // Invalid
                        System.out.println(ANSICol.RED + "Invalid selection" + ANSICol.RESET);
                    }
                }
            }
        } catch (IllegalStateException | NoSuchElementException e) {
            // System.in has been closed
            System.out.println("Forcibly exiting...");
            System.exit(1);
        }
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
        jsap.registerParameter(sw2);

        Switch sw3 = new Switch("exportall")
                .setShortFlag('a')
                .setLongFlag("exportall");
        sw3.setHelp("Sends all products to output CSV, even if not modified");
        jsap.registerParameter(sw3);

        Switch sw4 = new Switch("webserver")
                .setShortFlag('s')
                .setLongFlag("web");
        sw4.setHelp("Start web server");
        jsap.registerParameter(sw4);

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

        FlaggedOption opt4 = new FlaggedOption("webport")
                .setStringParser(IntegerStringParser.getParser())
                .setShortFlag('p')
                .setLongFlag("port");
        opt4.setHelp("Set web server port");


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

        lineEndings = config.getBoolean("winlineend") ? "\r\n" : "\n";

        // Load product file
        importProducts(config.getFile("file"), config.getBoolean("zerostock"), config.getBoolean("exportall"));
        if(config.getFile("resumetake") != null) importExistingStockTake(config.getFile("resumetake"));


        if (config.getBoolean("webserver")) {
            // Start web server
            WebServer.initiate(config.getInt("webport", 8080));
        } else {
            // Start graphical stock take
            graphicalStockTake(config.getFile("outputfile", new File("stock.csv")));
        }
    }
}

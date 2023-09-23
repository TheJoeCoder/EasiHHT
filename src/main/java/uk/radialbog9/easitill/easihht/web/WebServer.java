package uk.radialbog9.easitill.easihht.web;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import uk.radialbog9.easitill.easihht.Product;
import uk.radialbog9.easitill.easihht.StockTake;

import java.util.ArrayList;
import java.util.List;

public class WebServer {

    private static void sendReqHeaders(Context ctx) {
        ctx.contentType(ContentType.JSON);
        ctx.header("Access-Control-Allow-Origin", "*");
    }

    private static void sendNotFound(Context ctx) {
        ctx.status(404);
        ctx.contentType(ContentType.JSON);
        ctx.result("{\"text\":\"not found\"}");
    }

    private static void sendBadRequest(Context ctx) {
        ctx.status(400);
        ctx.contentType(ContentType.JSON);
        ctx.result("{\"text\":\"bad request\"}");
    }

    private static String getProductJson(Product prod) {
        return "{\"linecode\":" + prod.linecode
                + ",\"plu\":\"" + prod.plu
                + "\",\"description\":\"" + prod.description.replace("\"", "\\\"").replaceAll("\\p{Cc}", "")
                + "\",\"currentStockTakeLevel\":" + StockTake.getStock(prod)
                + "}";
    }

    public static void initiate(int port) {
        Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .get("/api", ctx -> {
                    sendReqHeaders(ctx);
                    ctx.result("{\"text\":\"api\"}");
                })
                .get("/api/item/plu", ctx -> {
                    sendReqHeaders(ctx);
                    String plu = ctx.queryParam("plu");
                    if (StockTake.getPluProductCache().containsKey(plu)) {
                        Product prod = StockTake.getPluProductCache().get(plu);
                        ctx.result(getProductJson(prod));
                    } else {
                        sendNotFound(ctx);
                    }
                })
                .get("/api/item/linecode", ctx -> {
                    sendReqHeaders(ctx);
                    String lc = ctx.queryParam("linecode");
                    int linecode = -1;
                    try {
                        assert lc != null;
                        linecode = Integer.parseInt(lc);
                    } catch (AssertionError | NumberFormatException e) {
                        sendBadRequest(ctx);
                    }
                    if (StockTake.getLinecodeProductCache().containsKey(linecode)) {
                        Product prod = StockTake.getLinecodeProductCache().get(linecode);
                        ctx.result(getProductJson(prod));
                    } else {
                        sendNotFound(ctx);
                    }
                })
                .get("/api/item/search", ctx -> {
                    sendReqHeaders(ctx);
                    String search = ctx.queryParam("query");
                    List<Product> results = new ArrayList<>();
                    if (search != null) {
                        for (Product prod : StockTake.getPluProductCache().values()) {
                            if (prod.description.toLowerCase().contains(search.toLowerCase())) {
                                results.add(prod);
                            }
                        }
                        StringBuilder json = new StringBuilder("{\"results\":[");
                        for (int i = 0; i < results.size(); i++) {
                            Product prod = results.get(i);
                            json.append(getProductJson(prod));
                            if (i != results.size() - 1) {
                                json.append(",");
                            }
                        }
                        json.append("]}");
                        ctx.result("{\"results\":" + json + "}");
                    } else {
                        sendBadRequest(ctx);
                    }
                    ctx.result("{\"text\":\"search\"}");
                })
                .post("/api/item/update", ctx -> {
                    sendReqHeaders(ctx);
                    String lc = ctx.formParam("linecode");
                    String st = ctx.formParam("stock");
                    if (lc != null && st != null) {
                        int linecode = -1;
                        int stock = -1;
                        try {
                            linecode = Integer.parseInt(lc);
                            stock = Integer.parseInt(st);
                        } catch (NumberFormatException e) {
                            sendBadRequest(ctx);
                        }
                        if(!StockTake.getLinecodeProductCache().containsKey(linecode)) {
                            sendNotFound(ctx);
                            return;
                        }
                        Product prod = StockTake.getLinecodeProductCache().get(linecode);
                        if (stock != -1) {
                            StockTake.setStock(prod, stock);
                            ctx.result(getProductJson(prod));
                        }
                    } else {
                        sendBadRequest(ctx);
                    }
                })
                .get("/api/saveandexit", ctx -> {
                    sendReqHeaders(ctx);
                    StockTake.exportStock(StockTake.getOutputFile());
                    ctx.result("{\"text\":\"saved\"}");
                    System.exit(0);
                })
                .start(port);
    }
}

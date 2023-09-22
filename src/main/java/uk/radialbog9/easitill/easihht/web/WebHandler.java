package uk.radialbog9.easitill.easihht.web;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import uk.radialbog9.easitill.easihht.Product;
import uk.radialbog9.easitill.easihht.StockTake;

import java.util.ArrayList;
import java.util.List;

public class WebHandler extends Handler.Abstract {
    private void sendNotFound(Response response, Callback callback) {
        response.setStatus(404);
        Content.Sink.write(response, true, "{\"text\":\"not found\"}", callback);
    }

    private void sendBadRequest(Response response, Callback callback) {
        response.setStatus(400);
        Content.Sink.write(response, true, "{\"text\":\"bad request\"}", callback);
    }

    private String getProductJson(Product prod) {
        return "{\"linecode\":" + prod.linecode
                + ",\"plu\":\"" + prod.plu
                + "\",\"description\":\"" + prod.description.replace("\"", "\\\"").replaceAll("\\p{Cc}", "")
                + "\",\"currentStockTakeLevel\":" + StockTake.getStock(prod)
                + "}";
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        response.setStatus(200);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
        String method = request.getMethod();
        String path = Request.getPathInContext(request);

        if (path.equals("/")) {
            Content.Sink.write(response, true, "{\"text\":\"hello world\"}", callback);
            return true;
        }

        if (path.equals("/api")) {
            Content.Sink.write(response, true, "{\"text\":\"api\"}", callback);
            return true;
        }

        if (path.equals("/api/item/plu") && method.equals("GET")) {
            Fields queryParams = Request.extractQueryParameters(request);
            Fields.Field pluField = queryParams.get("plu");
            if(pluField != null) {
                String plu = pluField.getValue();
                if (StockTake.getPluProductCache().containsKey(plu)) {
                    response.setStatus(200);
                    Product prod = StockTake.getPluProductCache().get(plu);
                    Content.Sink.write(response, true, getProductJson(prod), callback);
                } else {
                    sendNotFound(response, callback);
                }
            } else {
                sendBadRequest(response, callback);
            }
            return true;
        }

        if (path.equals("/api/item/search") && method.equals("GET")) {
            Fields queryParams = Request.extractQueryParameters(request);
            Fields.Field searchField = queryParams.get("query");
            List<Product> results = new ArrayList<>();
            if(searchField != null) {
                String search = searchField.getValue();
                for (Product prod : StockTake.getPluProductCache().values()) {
                    if (prod.description.toLowerCase().contains(search.toLowerCase())) {
                        results.add(prod);
                    }
                }
                response.setStatus(200);
                StringBuilder json = new StringBuilder("{\"results\":[");
                for (int i = 0; i < results.size(); i++) {
                    Product prod = results.get(i);
                    json.append(getProductJson(prod));
                    if (i != results.size() - 1) {
                        json.append(",");
                    }
                }
                json.append("]}");
                Content.Sink.write(response, true, json.toString(), callback);
            } else {
                sendBadRequest(response, callback);
            }
            return true;
        }

        if (path.equals("/api/item/linecode") && method.equals("GET")) {
            Fields queryParams = Request.extractQueryParameters(request);
            Fields.Field linecodeField = queryParams.get("linecode");
            if(linecodeField != null) {
                int linecode;
                try {
                    linecode = Integer.parseInt(linecodeField.getValue());
                } catch (NumberFormatException e) {
                    sendBadRequest(response, callback);
                    return true;
                }
                if (StockTake.getLinecodeProductCache().containsKey(linecode)) {
                    response.setStatus(200);
                    Product prod = StockTake.getLinecodeProductCache().get(linecode);
                    Content.Sink.write(response, true, getProductJson(prod), callback);
                } else {
                    sendNotFound(response, callback);
                }
            } else {
                sendBadRequest(response, callback);
            }
            return true;
        }

        if (path.equals("/api/item/update") && method.equals("POST")) {
            Fields queryParams = Request.extractQueryParameters(request);
            Fields.Field linecodeField = queryParams.get("linecode");
            Fields.Field stockField = queryParams.get("stock");
            if(linecodeField != null && stockField != null) {
                int linecode;
                int stock;
                try {
                    linecode = Integer.parseInt(linecodeField.getValue());
                    stock = Integer.parseInt(stockField.getValue());
                } catch (NumberFormatException e) {
                    response.setStatus(400);
                    Content.Sink.write(response, true, "{\"text\":\"bad request\"}", callback);
                    return true;
                }
                if (StockTake.getLinecodeProductCache().containsKey(linecode)) {
                    response.setStatus(200);
                    Product prod = StockTake.getLinecodeProductCache().get(linecode);
                    StockTake.setStock(prod, stock);
                    Content.Sink.write(response, true, getProductJson(prod), callback);
                } else {
                    response.setStatus(404);
                    Content.Sink.write(response, true, "{\"text\":\"not found\"}", callback);
                }
            } else {
                response.setStatus(400);
                Content.Sink.write(response, true, "{\"text\":\"bad request\"}", callback);
            }
            return true;
        }

        // 404
        response.setStatus(404);
        Content.Sink.write(response, true, "{\"text\":\"not found\"}", callback);
        return true;
    }
}

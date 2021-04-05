/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import nxt.Asset;
import nxt.Order;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public final class GetAllOpenAskOrders extends APIServlet.APIRequestHandler {

    static final GetAllOpenAskOrders instance = new GetAllOpenAskOrders();

    private GetAllOpenAskOrders() {
        super(new APITag[] {APITag.AE}, "firstIndex", "lastIndex", "includeAssetData");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        boolean includeAssetData = "true".equalsIgnoreCase(req.getParameter("includeAssetData"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray ordersData = includeAssetData
            ? getOrderDataWithAssetData(firstIndex, lastIndex)
            : getOrderData(firstIndex, lastIndex);

        response.put("openOrders", ordersData);
        return response;
    }

    private JSONArray getOrderData(int firstIndex, int lastIndex) {
        JSONArray ordersData = new JSONArray();

        try (DbIterator<Order.Ask> askOrders = Order.Ask.getAll(firstIndex, lastIndex)) {
            while (askOrders.hasNext()) {
                ordersData.add(JSONData.askOrder(askOrders.next()));
            }
        }

        return ordersData;
    }

    private JSONArray getOrderDataWithAssetData(int firstIndex, int lastIndex) {
        Map<Long, Order.Ask> asks = new HashMap<>();

        try (DbIterator<Order.Ask> askOrders = Order.Ask.getAll(firstIndex, lastIndex)) {
            while (askOrders.hasNext()) {
                Order.Ask ask = askOrders.next();
                asks.put(ask.getAssetId(), ask);
            }
        }

        JSONArray ordersData = new JSONArray();

        try (DbIterator<Asset> assets = Asset.getAssets(asks.keySet())) {
            while (assets.hasNext()) {
                Asset asset = assets.next();
                ordersData.add(JSONData.askOrder(asks.get(asset.getId()), asset));
            }
        }

        return ordersData;
    }
}

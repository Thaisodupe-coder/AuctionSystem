package com.auction.model.user;

import com.auction.model.auction.Auction;
import com.auction.model.item.Item;

public interface ISeller {
    /**
     * Tạo 1 phiên đấu giá cho sản phẩm
     * @param item : sản phẩm đấu giá
     * @param description : mô tả sản phẩm đấu giá
     * @param startPrice : giá tại thời điểm bắt đầu
     * @param startTime : thời gian bắt đầu phiên đấu giá
     * @param endTime : thời gian kết thúc phiên đấu giá
     * @return
     */
    Auction postItem(Item item, double startPrice, long startTime, long endTime);
}

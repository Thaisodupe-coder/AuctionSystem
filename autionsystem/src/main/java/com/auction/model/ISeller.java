package com.auction.model;

public interface ISeller {
    /**
     * Tạo 1 phiên đấu giá cho sản phẩm
     * @param name : tên sản phẩm, phiên đấu giá
     * @param description : mô tả sản phẩm đấu giá
     * @param startPrice : giá tại thời điểm bắt đầu
     * @param minutes : thời gian tới khi kết thúc phiên đấu giá (theo phút)
     * @return
     */
    Auction postItem(String name, String description,String category, double startPrice, int minutes);
}

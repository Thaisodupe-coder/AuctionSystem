package com.auction;

import com.auction.exception.AuctionClosedException;
import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.*;
import com.auction.model.user.*;
import com.auction.service.AuctionManager;
import java.time.LocalDateTime;
public class Main {
    public static void main(String[] args) {
        // Lấy instance của AuctionManager
        AuctionManager auctionManager = AuctionManager.getINSTANCE();

        // Tạo người dùng và vật phẩm
        Seller seller = new Seller("vu", "654321");
        Bidder bidder1 = new Bidder("thai", "123456");
        Bidder bidder2 = new Bidder("hoang", "dfghgfvcagw");
        Item item = new Vehicle("vinfast");
        // Người bán tạo phiên đấu giá
        Auction auction = seller.postItem(item, 10.0, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(1));
        
        auction.addObserver(bidder1);
        auction.addObserver(bidder2);
        System.out.println("bắt đầu đấu giá, giá khởi điểm: "+auction.getHighestBid());

        bidder1.placeBid(auction.getId(), 100);
        
        // Kiểm tra người thắng (Lúc này sẽ là null vì trạng thái là RUNNING)
        System.out.println("Người thắng hiện tại (Manager): " + auctionManager.getWinner(auction.getId()));

        // Giả lập kết thúc
        auction.setStatus(AuctionStatus.FINISHED);
        System.out.println("Sau khi kết thúc:");
        System.out.println("Số tiền thắng: " + auctionManager.getWinningAmount(auction.getId()));
        System.out.println("ID người thắng: " + auctionManager.getWinner(auction.getId()));

        try {
            System.out.println("\nThử đặt giá khi phiên đấu giá đã kết thúc...");
            auctionManager.placeBid(auction.getId(), bidder2.getId(), 100);
        } catch (AuctionClosedException e) {
            System.out.println("Đặt giá thất bại đúng như mong đợi: " + e.getMessage());
        }
    }
}

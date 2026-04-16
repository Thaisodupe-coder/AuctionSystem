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
        // Người bán tạo phiên đấu giá, đặt thời gian bắt đầu trong quá khứ và thời gian kết thúc trong tương lai gần
        // để mô phỏng phiên đấu giá đang chạy và sau đó kết thúc
        Auction auction = seller.postItem(item, 10.0, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusSeconds(2)); // Kết thúc sau 2 giây
        
        auction.addObserver(bidder1);
        auction.addObserver(bidder2);
        System.out.println("bắt đầu đấu giá, giá khởi điểm: "+auction.getHighestBid());

        bidder1.placeBid(auction.getId(), 100);
        
        // Kiểm tra người thắng (Lúc này sẽ là null vì trạng thái là RUNNING)
        System.out.println("Người thắng hiện tại (Manager): " + auctionManager.getWinnerId(auction.getId()));
        auction.setStatus(AuctionStatus.FINISHED);;

        // Chờ cho phiên đấu giá thực sự kết thúc theo thời gian
        // try {
        //     System.out.println("\nĐang chờ phiên đấu giá kết thúc...");
        //     Thread.sleep(3000); // Chờ 3 giây để đảm bảo thời gian kết thúc đã qua
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        //     System.err.println("Thread interrupted: " + e.getMessage());
        // }

        System.out.println("Sau khi kết thúc:");
        System.out.println("Số tiền thắng: " + auctionManager.getWinningBid(auction.getId()));
        System.out.println("ID người thắng: " + auctionManager.getWinnerId(auction.getId()));

        try {
            System.out.println("\nThử đặt giá khi phiên đấu giá đã kết thúc...");
            auctionManager.placeBid(auction.getId(), bidder2.getId(), 100);
        } catch (AuctionClosedException e) {
            System.out.println("Đặt giá thất bại đúng như mong đợi: " + e.getMessage());
        }
    }
}

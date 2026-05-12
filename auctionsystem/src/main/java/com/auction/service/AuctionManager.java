package com.auction.service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.Item;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.user.Seller;
import com.auction.model.user.NormalUser;
//quản lý các phiên đấu giá, xử lý các logic không thay đổi trạng thái của phiên đấu giá
public class AuctionManager {
    private static volatile AuctionManager INSTANCE;
    private Map<String, Auction> auctions = new ConcurrentHashMap<>(); //lưu trữ các phiên đấu giá
    private AuctionManager() {
        // --- Dữ liệu mồi (Seed data) để test UI ---
        Seller mockSeller = new Seller(new NormalUser("mockSeller", "123"));
        
        Item item1 = new Art("Mona Lisa", "Bức tranh nổi tiếng của Leonardo da Vinci");
        Auction auction1 = new Auction(item1, mockSeller, 500000.0, LocalDateTime.now().minusMinutes(30), LocalDateTime.now().plusHours(2));
        
        Item item2 = new Electronics("Macbook Pro", "Laptop cao cấp của Apple");
        Auction auction2 = new Auction(item2, mockSeller, 30000000.0, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusDays(1));
        
        auctions.put(auction1.getId(), auction1);
        auctions.put(auction2.getId(), auction2);
    }
    public static AuctionManager getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (AuctionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuctionManager();
                }
            }
        }
        return INSTANCE;
    }

    // Tạo phiên đấu giá mới
    // Auction sẽ tự quản lý trạng thái của nó thông qua phương thức updateAuctionStatus()
    public Auction createAuction(Item item, Seller seller, double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime);
        this.addAuction(auction);
        return auction;
    }

    // Thêm phiên đấu giá vào bộ nhớ
    public void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction cannot be null");
        }
        auctions.put(auction.getId(), auction);
    }

    // Hủy phiên đấu giá
    public synchronized boolean cancelAuction(String auctionId, String sellerId) {
        Auction auction = getAuction(auctionId);
        if (auction == null) {
            return false; // Không tìm thấy phiên đấu giá
        }
        // Auction sẽ tự kiểm tra quyền và trạng thái
        return auction.cancelAuction(sellerId);
    }
    // =========================================================================
    //GETTER
    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    // Lấy toàn bộ danh sách phiên đấu giá
    public java.util.List<Auction> getAllAuctions() {
        return new java.util.ArrayList<>(auctions.values());
    }

    // Lấy danh sách các phiên đấu giá do một Seller cụ thể tạo
    public java.util.List<Auction> getAuctionsBySeller(String sellerId) {
        return auctions.values().stream()
                .filter(auction -> auction.getSeller().getId().equals(sellerId))
                .collect(Collectors.toList());
    }

    // =========================================================================
    //xử lý logic
    /**
     *  đặt giá cho phiên đấu giá
     * @param auctionId id phiên đấu giá ()
     * @param bidderId  id người đặt giá
     * @param amount    giá trị được đặt
     * @return true nếu đặt giá thành công, false nếu đặt giá thất bại
     */
    public synchronized boolean placeBid(String auctionId, String bidderId, double amount) {
        Auction auction = getAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction with ID " + auctionId + " not found.");
        }
        // Ủy quyền xử lý đặt giá cho Auction
        // Auction sẽ tự kiểm tra trạng thái và tính hợp lệ của giá
        return auction.processBid(bidderId, amount);
    }

    // Trả về id người thắng (logic dịch vụ phiên đấu giá, chỉ trả về thông tin)
    public String getWinnerId(String auctionId) {
        Auction auction = getAuction(auctionId);
        if (auction == null) return null;

        // Chỉ có người thắng khi trạng thái là FINISHED hoặc PAID
        if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.PAID) {
            return auction.getHighestBidderId();
        }
        return null; 
    }

    // Trả về số tiền thắng cao nhất
    public double getWinningBid(String auctionId) {
        Auction auction = getAuction(auctionId);
        if (auction == null) return 0.0;
        
        if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.PAID) {
            return auction.getHighestBid();
        }
        return 0.0;
    }

}

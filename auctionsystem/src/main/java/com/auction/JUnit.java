package com.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.auction.exception.AuctionClosedException;
import com.auction.exception.InvalidBidException;
import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.item.Vehicle;
import com.auction.model.item.Item;
import com.auction.model.user.Bidder;
import com.auction.model.user.Seller;
import com.auction.model.user.NormalUser;
import com.auction.service.AuctionManager;

public class JUnit {
    public AuctionManager auctionManager = AuctionManager.getINSTANCE();

    @Test
    void bidSuccessWhenRunning() {
        // Expected output: Đặt giá thành công, cập nhật highestBid + highestBidderId, kiểm tra lịch sử đấu giá.
        Item item = new Art("Mona Lisa");
        Seller seller = new Seller(new NormalUser("Nguyễn Quốc Thái", "123456"));
        Auction auction = new Auction(
                item,
                seller,
                100.0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(5));

        Bidder bidder = new Bidder(new NormalUser("Đinh Anh Vũ", "654321"));
        boolean status = auction.processBid(bidder.getId(), 150.0);
        
        assertTrue(status);
        assertEquals(150.0, auction.getHighestBid());
        assertEquals(bidder.getId(), auction.getHighestBidderId());
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    void bidFailWhenLowerPrice() {
        // Expected output: Nem InvalidBidException khi giá đặt thấp hơn giá cao nhất hiện tại.
        Item item = new Art("The Scream");
        Seller seller = new Seller(new NormalUser("Nguyễn Viết Thông", "depzai va dang cap"));
        Auction auction = new Auction(
                item,
                seller,
                200.0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(5));
        
        Bidder bidder = new Bidder(new NormalUser("Phạm Hữu Chí Thành", "686868"));
        assertThrows(InvalidBidException.class, () -> auction.processBid(bidder.getId(), 150.0));
    }

    @Test
    void bidFailWhenNotRunning() {
        // Expected output: Ném AuctionClosedException khi phiên đấu giá chưa RUNNING.
        Item item = new Art("Starry Nights");
        Seller seller = new Seller(new NormalUser("Nguyễn Viết Thông", "Rauma"));
        Auction auction = new Auction(
                item,
                seller,
                100.0,
                LocalDateTime.now().plusMinutes(2),
                LocalDateTime.now().plusHours(1));

        Bidder bidder = new Bidder(new NormalUser("Nguyễn Quốc Thái", "thaidui123"));
        assertThrows(AuctionClosedException.class, () -> auction.processBid(bidder.getId(), 120.0));
    }

    @Test
    void cancelSuccessForOwner() {
        // Expected output: Huỷ thành công, trạng thái = CANCELED.
        Item item = new Vehicle("VinFast VF3");
        Seller seller = new Seller(new NormalUser("Đinh Anh Vũ", "654321"));
        Auction auction = new Auction(
                item,
                seller,
                50.0,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusHours(1));

        boolean canceled = auction.cancelAuction(seller.getId());

        assertTrue(canceled);
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
    }

    @Test
    void cancelFailWhenNotOwner() {
        // Expected output: Huỷ thất bại do người dùng ko phải chủ phiên.
        Item item = new Electronics("Điều hoà siêu mát");
        Seller owner = new Seller(new NormalUser("Nguyễn Quốc Thái", "123456"));
        Seller otherSeller = new Seller(new NormalUser("Nguyễn Viết Thông", "363636"));
        Auction auction = new Auction(
                item,
                owner,
                80.0,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusHours(1));

        boolean canceled = auction.cancelAuction(otherSeller.getId());

        assertTrue(!canceled);
        assertEquals(AuctionStatus.OPEN, auction.getStatus());
    }

    @Test
    void createAuctionStoredInManager() {
        // Expected output: Tạo auction thành công và getAuction trả về đúng object.
        Item item = new Electronics("Laptop");
        Seller seller = new Seller(new NormalUser("Đinh Anh Vũ", "presidentSVM"));

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                300.0,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(1));

        assertNotNull(auction);
        assertEquals(auction, auctionManager.getAuction(auction.getId()));
    }

    @Test
    void addNullAuctionThrowsException() {
        // Expected output: Nem IllegalArgumentException khi thêm auction null.
        assertThrows(IllegalArgumentException.class, () -> auctionManager.addAuction(null));
    }

    @Test
    void winnerInfoWhenFinished() {
        // Expected output: Có winnerId và winningBid đúng sau khi FINISHED.
        Item item = new Electronics("SamSung Galaxy S21");
        Seller seller = new Seller(new NormalUser("Nguyễn Viết Thông", "thichrauma"));

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                100.0,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(2));
        
        Bidder bidder = new Bidder(new NormalUser("Đinh Anh Vũ", "654321"));

        boolean status = auctionManager.placeBid(auction.getId(), bidder.getId(), 130.0);
        assertTrue(status);
        auction.setStatus(AuctionStatus.FINISHED);

        assertEquals(bidder.getId(), auctionManager.getWinnerId(auction.getId()));
        assertEquals(130.0, auctionManager.getWinningBid(auction.getId()));
    }

    @Test
    void defaultWinnerInfoWhenAuctionMissing() {
        // Expected output: winnerId = null, winningBid = 0.0 khi auction ko tồn tại.
        AuctionManager manager = AuctionManager.getINSTANCE();

        assertNull(manager.getWinnerId("123456754321"));
        assertEquals(0.0, manager.getWinningBid("1234567654321"));
    }

    @Test
    void bidderPlaceBidSuccess() {
        // Expected output: Bidder placeBid thành công và cập nhật giá cao nhất.
        Seller seller = new Seller(new NormalUser("Phạm Hữu Chí Thành", "camonquykhach"));
        Bidder bidder = new Bidder(new NormalUser("Nguyễn Quốc Thái", "thaidui123"));
        Item item = new Electronics("Điện thoại iPhone 17 Pro Max");

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                200.0,
                LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(5));

        boolean status = bidder.placeBid(auction.getId(), 250.0);

        assertTrue(status);
        assertEquals(bidder.getId(), auction.getHighestBidderId());
        assertEquals(250.0, auction.getHighestBid());
    }

    @Test
    void bidderPlaceBidFailWhenClosed() {
        // Expected output: Bidder placeBid ném AuctionClosedException khi phiên đấu giá đã đóng.
        Seller seller = new Seller(new NormalUser("Đinh Anh Vũ", "654321"));
        Bidder bidder = new Bidder(new NormalUser("Nguyễn Viết Thông", "rauma123"));
        Item item = new Vehicle("Ferrari F8");

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                300.0,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1));

        assertThrows(AuctionClosedException.class, () -> bidder.placeBid(auction.getId(), 320.0));
    }

    /**
     * Chay tat ca test theo kieu try assertion:
     * - PASS: in [PASS]
     * - sai assert: in [ASSERT FAIL]
     * - loi khac: in [ERROR]
     */
    public void runAll() {
        runCase("bidSuccessWhenRunning", this::bidSuccessWhenRunning);
        runCase("bidFailWhenLowerPrice", this::bidFailWhenLowerPrice);
        runCase("bidFailWhenNotRunning", this::bidFailWhenNotRunning);
        runCase("cancelSuccessForOwner", this::cancelSuccessForOwner);
        runCase("cancelFailWhenNotOwner", this::cancelFailWhenNotOwner);
        runCase("createAuctionStoredInManager", this::createAuctionStoredInManager);
        runCase("addNullAuctionThrowsException", this::addNullAuctionThrowsException);
        runCase("winnerInfoWhenFinished", this::winnerInfoWhenFinished);
        runCase("defaultWinnerInfoWhenAuctionMissing", this::defaultWinnerInfoWhenAuctionMissing);
        runCase("bidderPlaceBidSuccess", this::bidderPlaceBidSuccess);
        runCase("bidderPlaceBidFailWhenClosed", this::bidderPlaceBidFailWhenClosed);
    }

    private void runCase(String name, Runnable testMethod) {
        try {
            testMethod.run();
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            System.out.println("[ASSERT FAIL] " + name + " -> " + e.getMessage());
        } catch (Throwable t) {
            System.out.println("[ERROR] " + name + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
        } finally {
            System.out.println("_".repeat(50));
        }
    }
}

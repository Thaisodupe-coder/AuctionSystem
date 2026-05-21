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
import com.auction.model.user.Seller;
import com.auction.model.user.NormalUser;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;

public class AuctionSystemTest {
    public AuctionManager auctionManager = AuctionManager.getINSTANCE();
    public UserManager userManager = UserManager.getINSTANCE();

    @Test
    void bidSuccessWhenRunning() {
        // Expected output: Đặt giá thành công, cập nhật highestBid + highestBidderId,
        // kiểm tra lịch sử đấu giá.
        Item item = new Art("Mona Lisa", "Bức tranh nổi tiếng của Leonardo da Vinci");
        NormalUser sellerUser = userManager.register("Nguyễn Quốc Thái_" + System.currentTimeMillis(), "123456");
        Seller seller = new Seller(sellerUser);

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                100.0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(5)); // Sử dụng AuctionManager để tạo đấu giá

        NormalUser bidderUser = userManager.register("Đinh Anh Vũ_" + System.currentTimeMillis(), "654321");
        userManager.addBalance(bidderUser.getId(), 1000.0); // Cần nạp tiền để đủ điều kiện bid

        boolean status = auction.processBid(bidderUser.getId(), 150.0);

        assertTrue(status);
        assertEquals(150.0, auction.getHighestBid());
        assertEquals(bidderUser.getId(), auction.getHighestBidderId());
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    void bidFailWhenLowerPrice() {
        // Expected output: Nem InvalidBidException khi giá đặt thấp hơn giá cao nhất
        // hiện tại.
        Item item = new Art("The Scream", "Bức tranh nổi tiếng của Edvard Munch");
        NormalUser sellerUser = userManager.register("Nguyễn Viết Thông_" + System.currentTimeMillis(), "36nemchua");
        Seller seller = new Seller(sellerUser);
        Auction auction = auctionManager.createAuction(
                item,
                seller,
                200.0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(5));

        NormalUser bidderUser = userManager.register("Phạm Hữu Chí Thành_" + System.currentTimeMillis(), "686868");
        userManager.addBalance(bidderUser.getId(), 1000.0);

        assertThrows(InvalidBidException.class, () -> auction.processBid(bidderUser.getId(), 150.0));
    }

    @Test
    void bidFailWhenNotRunning() {
        // Expected output: Ném AuctionClosedException khi phiên đấu giá chưa RUNNING.
        Item item = new Art("Starry Nights", "Bức tranh nổi tiếng của Vincent van Gogh");
        NormalUser sellerUser = userManager.register("Nguyễn Viết Thông_" + System.currentTimeMillis(), "Rauma");
        Seller seller = new Seller(sellerUser);
        Auction auction = auctionManager.createAuction(
                item,
                seller,
                100.0,
                LocalDateTime.now().plusMinutes(2),
                LocalDateTime.now().plusHours(1));

        NormalUser bidderUser = userManager.register("Nguyễn Quốc Thái_" + System.currentTimeMillis(), "thaidui123");
        userManager.addBalance(bidderUser.getId(), 1000.0);

        assertThrows(AuctionClosedException.class, () -> auction.processBid(bidderUser.getId(), 120.0));
    }

    @Test
    void cancelSuccessForOwner() {
        // Expected output: Huỷ thành công, trạng thái = CANCELED.
        Item item = new Vehicle("VinFast VF3", "Xe máy điện của VinFast");
        NormalUser sellerUser = userManager.register("Đinh Anh Vũ_" + System.currentTimeMillis(), "654321");
        Seller seller = new Seller(sellerUser);
        Auction auction = auctionManager.createAuction(
                item,
                seller,
                50.0,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusHours(1));

        boolean canceled = auction.cancelAuction(sellerUser.getId());

        assertTrue(canceled);
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
    }

    @Test
    void cancelFailWhenNotOwner() {
        // Expected output: Huỷ thất bại do người dùng ko phải chủ phiên.
        Item item = new Electronics("Điều hoà siêu mát", "Điều hoà công nghệ mới");
        NormalUser ownerUser = userManager.register("Nguyễn Quốc Thái_" + System.currentTimeMillis(), "123456");
        Seller owner = new Seller(ownerUser);

        NormalUser otherUser = userManager.register("Nguyễn Viết Thông_" + System.currentTimeMillis(), "363636");

        Auction auction = auctionManager.createAuction(
                item,
                owner,
                80.0,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusHours(1));

        boolean canceled = auction.cancelAuction(otherUser.getId());

        assertTrue(!canceled);
        assertEquals(AuctionStatus.OPEN, auction.getStatus());
    }

    @Test
    void createAuctionStoredInManager() {
        // Expected output: Tạo auction thành công và getAuction trả về đúng object.
        Item item = new Electronics("Laptop", "Laptop cao cấp");
        NormalUser sellerUser = userManager.register("Đinh Anh Vũ_" + System.currentTimeMillis(), "presidentSVM");
        Seller seller = new Seller(sellerUser);

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
        Item item = new Electronics("SamSung Galaxy S21", "Điện thoại cao cấp");
        NormalUser sellerUser = userManager.register("Nguyễn Viết Thông_" + System.currentTimeMillis(), "thichrauma");
        Seller seller = new Seller(sellerUser);

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                100.0,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(5));

        NormalUser bidderUser = userManager.register("Đinh Anh Vũ_" + System.currentTimeMillis(), "654321");
        userManager.addBalance(bidderUser.getId(), 1000.0);

        boolean status = auctionManager.placeBid(auction.getId(), bidderUser.getId(), 130.0);
        assertTrue(status);
        auction.setStatus(AuctionStatus.FINISHED);

        assertEquals(bidderUser.getId(), auctionManager.getWinnerId(auction.getId()));
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
        NormalUser sellerUser = userManager.register("Phạm Hữu Chí Thành_" + System.currentTimeMillis(),
                "camonquykhach");
        Seller seller = new Seller(sellerUser);

        NormalUser bidderUser = userManager.register("Nguyễn Quốc Thái_" + System.currentTimeMillis(), "thaidui123");
        userManager.addBalance(bidderUser.getId(), 1000.0);

        Item item = new Electronics("Điện thoại iPhone 17 Pro Max", "Điện thoại cao cấp");

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                200.0,
                LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(5));

        boolean status = auctionManager.placeBid(auction.getId(), bidderUser.getId(), 250.0);

        assertTrue(status);
        assertEquals(bidderUser.getId(), auction.getHighestBidderId());
        assertEquals(250.0, auction.getHighestBid());
    }

    @Test
    void bidderPlaceBidFailWhenClosed() {
        // Expected output: placeBid ném AuctionClosedException khi phiên đấu giá
        // đã đóng.
        NormalUser sellerUser = userManager.register("Đinh Anh Vũ_" + System.currentTimeMillis(), "654321");
        Seller seller = new Seller(sellerUser);

        NormalUser bidderUser = userManager.register("Nguyễn Viết Thông_" + System.currentTimeMillis(), "rauma123");
        userManager.addBalance(bidderUser.getId(), 1000.0);

        Item item = new Vehicle("Ferrari F8", "Xe thể thao cao cấp");

        Auction auction = auctionManager.createAuction(
                item,
                seller,
                300.0,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(5));

        assertThrows(AuctionClosedException.class,
                () -> auctionManager.placeBid(auction.getId(), bidderUser.getId(), 320.0));
    }
}

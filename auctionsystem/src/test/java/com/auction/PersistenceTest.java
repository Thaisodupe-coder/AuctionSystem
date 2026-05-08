package com.auction;

import com.auction.model.auction.Auction;
import com.auction.model.item.*;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;
import com.auction.util.PersistenceService;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceTest {

    @Test
    void testSaveAndLoadIntegration() throws Exception {
        // 1. Chuẩn bị dữ liệu mẫu trên RAM
        UserManager userManager = UserManager.getINSTANCE();
        AuctionManager auctionManager = AuctionManager.getINSTANCE();

        String testUsername = "test_persistence_user_" + System.currentTimeMillis();
        NormalUser registeredUser = userManager.register(testUsername, "password123");
        userManager.addBalance(registeredUser.getId(), 5000.0);

        Item item = new Art("Laptop Gaming", "Core i9, 32GB RAM");
        Seller seller = userManager.getSellerRole(registeredUser);
        Auction auction = auctionManager.createAuction(item, seller, 1000.0, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        // 2. Thực hiện LƯU dữ liệu xuống JSON
        PersistenceService.saveData();

        // Kiểm tra vật lý: File có tồn tại không?
        assertTrue(new File("data/users.json").exists(), "File users.json phải tồn tại trong thư mục data");
        assertTrue(new File("data/auctions.json").exists(), "File auctions.json phải tồn tại trong thư mục data");

        // 3. XÓA TRẮNG dữ liệu trên RAM (Sử dụng Reflection để clear các Map private)
        clearPrivateMap(userManager, "users");
        clearPrivateMap(auctionManager, "auctions");

        // Xác nhận RAM đã trống
        assertNull(userManager.getUserById(registeredUser.getId()), "RAM phải trống sau khi clear map");
        assertNull(auctionManager.getAuction(auction.getId()), "RAM phải trống sau khi clear map");

        // 4. Thực hiện NẠP lại dữ liệu từ JSON
        PersistenceService.loadData();

        // 5. KIỂM TRA: Dữ liệu có quay trở lại RAM đúng như ban đầu không?
        NormalUser restoredUser = userManager.getUserById(registeredUser.getId());
        assertNotNull(restoredUser, "User phải được khôi phục từ file JSON");
        assertEquals(5000.0, restoredUser.getBalance(), "Số dư phải được khôi phục đúng");
        assertNotNull(auctionManager.getAuction(auction.getId()), "Phiên đấu giá phải được khôi phục từ file JSON");
    }

    private void clearPrivateMap(Object manager, String fieldName) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) field.get(manager);
        map.clear();
    }
}

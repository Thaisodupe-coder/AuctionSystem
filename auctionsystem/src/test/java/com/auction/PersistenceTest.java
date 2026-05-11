package com.auction;

import com.auction.model.auction.Auction;
import com.auction.model.item.*;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;
import com.auction.util.PersistenceService;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceTest {

    @Test
    void testPostgresSaveAndLoad() throws Exception {
        // 0. Đảm bảo RAM sạch trước khi test để tránh nhiễu dữ liệu cũ
        System.out.println("[Test] Bắt đầu dọn dẹp RAM...");
        UserManager userManager = UserManager.getINSTANCE();
        AuctionManager auctionManager = AuctionManager.getINSTANCE();
        clearPrivateMap(userManager, "users");
        clearPrivateMap(auctionManager, "auctions");

        // 1. Chuẩn bị dữ liệu mẫu
        System.out.println("[Test] Đang chuẩn bị dữ liệu mẫu...");
        String testUsername = "test_persistence_user_" + System.currentTimeMillis();
        NormalUser registeredUser = userManager.register(testUsername, "password123");
        userManager.addBalance(registeredUser.getId(), 5000.0);

        Item item = new Art("Laptop Gaming", "Core i9, 32GB RAM");
        Seller seller = userManager.getSellerRole(registeredUser);
        Auction auction = auctionManager.createAuction(item, seller, 1000.0, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        // 2. Thực hiện LƯU dữ liệu xuống PostgreSQL (Upsert)
        System.out.println("[Test] Đang lưu dữ liệu xuống PostgreSQL...");
        PersistenceService.saveData();

        // 3. XÓA TRẮNG dữ liệu trên RAM (Sử dụng Reflection để clear các Map private)
        System.out.println("[Test] Đang xóa trắng RAM để chuẩn bị nạp lại...");
        clearPrivateMap(userManager, "users");
        clearPrivateMap(auctionManager, "auctions");

        // Xác nhận RAM đã trống
        assertNull(userManager.getUserById(registeredUser.getId()), "RAM phải trống sau khi clear map");
        assertNull(auctionManager.getAuction(auction.getId()), "RAM phải trống sau khi clear map");

        // 4. Thực hiện NẠP lại dữ liệu từ PostgreSQL
        System.out.println("[Test] Đang nạp lại dữ liệu từ PostgreSQL...");
        PersistenceService.loadData();

        // 5. KIỂM TRA: Dữ liệu có được khôi phục chính xác không?
        System.out.println("[Test] Đang kiểm tra dữ liệu sau khi nạp...");
        NormalUser restoredUser = userManager.getUserById(registeredUser.getId());
        assertNotNull(restoredUser, "User phải được khôi phục từ PostgreSQL");
        assertEquals(5000.0, restoredUser.getBalance(), 0.001, "Số dư phải được khôi phục đúng");
        assertNotNull(auctionManager.getAuction(auction.getId()), "Phiên đấu giá phải được khôi phục từ PostgreSQL");
        
        System.out.println("[Test] KẾT QUẢ: Test thành công rực rỡ!");
    }

    private void clearPrivateMap(Object manager, String fieldName) throws Exception {
        Class<?> clazz = manager.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Map<?, ?> map = (Map<?, ?>) field.get(manager);
                map.clear();
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }
}

package com.auction.util;

import com.auction.model.auction.Auction;
import com.auction.model.item.Art;
import com.auction.model.item.Item;
import com.auction.model.user.NormalUser;
import com.auction.model.user.User;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceService {
    // Quy ước các file JSON sẽ nằm trong thư mục 'data' ở thư mục gốc của dự án
    private static final String DATA_DIR = "data";
    private static final String USER_FILE = DATA_DIR + "/users.json";
    private static final String AUCTION_FILE = DATA_DIR + "/auctions.json";

    // Cấu hình Gson để xử lý được kiểu thời gian LocalDateTime của Java 8
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            // Xử lý đa hình cho Item: Tự động nhận diện loại Item dựa trên cấu trúc hoặc dữ liệu
            .registerTypeAdapter(Item.class, (JsonDeserializer<Item>) (json, typeOfT, context) -> {
                JsonObject jsonObject = json.getAsJsonObject();
                // Nếu bạn có nhiều loại, bạn có thể kiểm tra các trường đặc trưng ở đây
                // Ví dụ đơn giản: Nếu không có gì đặc biệt thì coi là Art, 
                // hoặc dựa vào một field "className" nếu bạn muốn làm chuyên nghiệp hơn.
                return context.deserialize(json, Art.class); 
            })
            // Xử lý lớp trừu tượng User: Mặc định coi là NormalUser
            .registerTypeAdapter(User.class, (JsonDeserializer<User>) (json, typeOfT, context) -> 
                    context.deserialize(json, NormalUser.class))
            .setPrettyPrinting()
            .create();
    /**
     * Tự động nạp dữ liệu từ file vào các Map private của Manager
     */
    public static void loadData() {
        ensureDataDirectoryExists(); // Đảm bảo thư mục data tồn tại
        loadMapFromFile(USER_FILE, UserManager.getINSTANCE(), "users", new TypeToken<ConcurrentHashMap<String, NormalUser>>(){});
        loadMapFromFile(AUCTION_FILE, AuctionManager.getINSTANCE(), "auctions", new TypeToken<ConcurrentHashMap<String, Auction>>(){});
        System.out.println("[Persistence] Hoàn tất nạp dữ liệu hệ thống.");
    }
    /**
     * Lưu toàn bộ dữ liệu từ các Map trong Manager xuống file
     */
    public static void saveData() {
        ensureDataDirectoryExists(); // Đảm bảo thư mục data tồn tại
        saveMapToFile(USER_FILE, UserManager.getINSTANCE(), "users");
        saveMapToFile(AUCTION_FILE, AuctionManager.getINSTANCE(), "auctions");
        System.out.println("[Persistence] Hoàn tất lưu dữ liệu hệ thống.");
    }

    /**
     * Đảm bảo thư mục DATA_DIR tồn tại. Nếu không, sẽ tạo mới.
     */
    private static void ensureDataDirectoryExists() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs(); // Tạo thư mục và các thư mục cha nếu cần
        }
    }

    private static void loadMapFromFile(String fileName, Object manager, String fieldName, TypeToken<?> typeToken) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("[Persistence] Không tìm thấy file: " + file.getAbsolutePath());
            return;
        }

        System.out.println("[Persistence] Đang nạp từ: " + file.getAbsolutePath());

        try (Reader reader = new FileReader(file)) {
            Map<?, ?> loadedData = gson.fromJson(reader, typeToken.getType());
            if (loadedData != null) {
                // Kỹ thuật Reflection: Truy cập vào biến private Map
                Field field = manager.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Map targetMap = (Map) field.get(manager);
                targetMap.putAll(loadedData);
            }
        } catch (Exception e) {
            System.err.println("Lỗi load " + fileName + ": " + e.getMessage());
        }
    }

    private static void saveMapToFile(String fileName, Object manager, String fieldName) {
        try (Writer writer = new FileWriter(fileName)) {
            Field field = manager.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object data = field.get(manager);
            gson.toJson(data, writer);
        } catch (Exception e) {
            System.err.println("Lỗi save " + fileName + ": " + e.getMessage());
        }
    }
}

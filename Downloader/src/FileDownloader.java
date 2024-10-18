import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class FileDownloader {
    private static final Map<String, String> EXTENSIONS = new HashMap<>();
    private final ExecutorService executor;
    static {
        EXTENSIONS.put("FF D8 FF", ".jpg");
        EXTENSIONS.put("89 50 4E 47", ".png");
        EXTENSIONS.put("49 44 33", ".mp3");
        EXTENSIONS.put("25 50 44 46", ".pdf");
        EXTENSIONS.put("00 00 00 20", ".mp4");
        EXTENSIONS.put("50 4B 03 04", ".docx");
    }

    public FileDownloader(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void downloadFile(String url, String directory) {
        executor.submit(() -> {
            try {
                URLConnection conn = new URL(url).openConnection();
                InputStream is = conn.getInputStream();
                byte[] header = new byte[4];
                is.read(header);

                String extension = getExtension(header);
                if (extension == null) {
                    System.err.println("Не удалось определить тип файла для URL: " + url);
                    return;
                }
                String fullPath = directory + "file_" + System.currentTimeMillis() + extension;
                saveFile(fullPath, is, header);
                openFile(fullPath);
            } catch (IOException e) {
                System.err.println("Ошибка при скачивании файла из " + url + ": " + e.getMessage());
            }
        });
    }
    private void saveFile(String path, InputStream is, byte[] header) throws IOException {
        File dir = new File(path).getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Не удалось создать директорию: " + dir.getAbsolutePath());
        }
        try (OutputStream os = new FileOutputStream(path)) {
            os.write(header);
            os.write(is.readAllBytes());
        }
        System.out.println("Файл скачан: " + path);
    }
    private void openFile(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            System.err.println("Не удалось открыть файл " + path + ": " + e.getMessage());
        }
    }
    private String getExtension(byte[] header) {
        String hexString = bytesToHex(header);
        return EXTENSIONS.getOrDefault(hexString, null);
    }
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    public void shutdown() {
        executor.shutdown();
    }
    public static void main(String[] args) {
        FileDownloader downloader = new FileDownloader(4);
        String downloadDirectory = "downloads/";
        downloader.downloadFile("https://s2.save4k.ru/save/ef7c412e/file1/youtube/134/johcE5s525M/Gachimuchi%20Original.mp4", downloadDirectory);
        downloader.downloadFile("https://yakovtsev.ru/files/Old_New_meme_2023.pdf", downloadDirectory);
        downloader.downloadFile("https://rus.hitmotop.com/get/music/20211201/Smeshariki_-_SHUBIDU_-_Obrabotka_GachinSkiy_Gachi_Remix_Gachi_Right_Gachi_gachi_-_SHUBIDU_Original_73411954.mp3", downloadDirectory);
        downloader.downloadFile("https://cs13.pikabu.ru/avatars/3111/x3111159-1891971161.png", downloadDirectory);
        downloader.shutdown();
        System.out.println("Все загрузки завершены.");
    }
}


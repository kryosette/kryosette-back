package com.example.demo.converters.image_to_text;
//
//import jakarta.annotation.PostConstruct;
//import net.sourceforge.tess4j.ITesseract;
//import net.sourceforge.tess4j.Tesseract;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.File;
//
//@RestController
//@RequestMapping("ocr")
//public class OcrController {
//
//    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);
//
//    @PostConstruct
//    public void init() {
//        // Установка пути к tessdata
//        String tessDataPath = new File("src/main/resources/tessdata").getAbsolutePath();
//        System.setProperty("TESSDATA_PREFIX", tessDataPath);
//        logger.info("TESSDATA_PREFIX set to: {}", tessDataPath);
//    }
//
//    @PostMapping("/convert")
//    public ResponseEntity<String> convertImageToText(
//            @RequestParam("image") MultipartFile file) {
//
//        File imageFile = null;
//        try {
//            // 1. Проверка файла
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().body("Файл пуст");
//            }
//
//            // 2. Проверка существования tessdata
//            File tessDataDir = new File("src/main/resources/tessdata");
//            if (!tessDataDir.exists()) {
//                String error = "Папка tessdata не найдена: " + tessDataDir.getAbsolutePath();
//                logger.error(error);
//                return ResponseEntity.internalServerError().body(error);
//            }
//
//            // 3. Проверка наличия language files
//            File engFile = new File(tessDataDir, "eng.traineddata");
//            File rusFile = new File(tessDataDir, "rus.traineddata");
//
//            if (!engFile.exists() || !rusFile.exists()) {
//                String error = "Файлы языка не найдены. Проверьте: " +
//                        engFile.getAbsolutePath() + " и " +
//                        rusFile.getAbsolutePath();
//                logger.error(error);
//                return ResponseEntity.internalServerError().body(error);
//            }
//
//            // 4. Конвертация в изображение
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            if (image == null) {
//                return ResponseEntity.badRequest().body("Неподдерживаемый формат изображения");
//            }
//
//            // 5. Сохранение во временный файл
//            imageFile = File.createTempFile("ocr-", ".png");
//            ImageIO.write(image, "png", imageFile);
//
//            // 6. Настройка Tesseract
//            ITesseract tesseract = new Tesseract();
//            tesseract.setDatapath(tessDataDir.getAbsolutePath());
//            tesseract.setLanguage("rus+eng");
//
//            // 7. Выполнение OCR
//            String result = tesseract.doOCR(imageFile);
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            logger.error("OCR ошибка", e);
//            return ResponseEntity.internalServerError()
//                    .body("Ошибка OCR: " + e.getMessage());
//        } finally {
//            if (imageFile != null && imageFile.exists()) {
//                imageFile.delete();
//            }
//        }
//    }
//}